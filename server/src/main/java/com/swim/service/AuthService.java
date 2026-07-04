package com.swim.service;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.swim.common.BusinessException;

import com.swim.dto.*;

import com.swim.entity.CoachAuthorization;

import com.swim.entity.User;

import com.swim.mapper.CoachAuthorizationMapper;

import com.swim.mapper.UserMapper;

import com.swim.util.JwtUtil;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import java.util.Map;



@Slf4j

@Service

@RequiredArgsConstructor

public class AuthService {

    public static final String ROLE_STUDENT = "student";

    public static final String ROLE_COACH = "coach";

    private static final String DEFAULT_STROKE_ORDER = "[\"自由泳\",\"蛙泳\",\"仰泳\",\"蝶泳\"]";



    private final WxService wxService;

    private final UserMapper userMapper;

    private final CoachAuthorizationMapper coachAuthMapper;

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();



    public LoginResponse wxLogin(String code) {

        log.info("wxLogin 开始: code={}", code);

        String openid = wxService.code2Openid(code);

        log.info("wxLogin 拿到 openid={}", openid);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {

            log.info("wxLogin 新用户，创建记录: openid={}", openid);

            user = new User();

            user.setOpenid(openid);

            user.setStrokeOrder(DEFAULT_STROKE_ORDER);

            user.setCreatedAt(LocalDateTime.now());

            userMapper.insert(user);

        } else {

            log.info("wxLogin 老用户: id={}, role={}", user.getId(), user.getRole());

        }

        String token = jwtUtil.generateToken(user.getId());

        boolean needBindPhone = user.getPhone() == null || user.getPhone().isBlank();

        boolean needSelectRole = user.getRole() == null || user.getRole().isBlank();

        log.info("wxLogin 完成: userId={}, needBindPhone={}, needSelectRole={}",

                user.getId(), needBindPhone, needSelectRole);

        return new LoginResponse(token, needBindPhone, needSelectRole, user.getRole());

    }



    public Map<String, String> bindPhone(Long userId, BindPhoneRequest req) {

        String phone = wxService.getPhoneNumber(req.getCode());

        User user = userMapper.selectById(userId);

        if (user == null) {

            throw new BusinessException(404, "用户不存在");

        }

        user.setPhone(phone);

        userMapper.updateById(user);

        return Map.of("phone", phone);

    }



    public ProfileVO getProfile(Long userId) {

        User user = requireUser(userId);

        ProfileVO vo = new ProfileVO();

        vo.setNickname(user.getNickname());

        vo.setPhone(maskPhone(user.getPhone()));

        vo.setRole(user.getRole());

        vo.setGender(user.getGender());

        vo.setBirthMonth(user.getBirthMonth());

        return vo;

    }



    public ProfileVO updateProfile(Long userId, ProfileUpdateRequest req) {

        User user = requireUser(userId);

        if (req.getNickname() != null) {

            user.setNickname(req.getNickname().trim());

        }

        if (req.getPhone() != null) {

            if (!req.getPhone().matches("^1\\d{10}$")) {

                throw new BusinessException(400, "手机号格式不正确");

            }

            user.setPhone(req.getPhone());

        }

        if (req.getGender() != null) {

            if (req.getGender() != 1 && req.getGender() != 2) {

                throw new BusinessException(400, "性别无效");

            }

            user.setGender(req.getGender());

        }

        if (req.getBirthMonth() != null) {

            user.setBirthMonth(req.getBirthMonth());

        }

        userMapper.updateById(user);

        return getProfile(userId);

    }



    public ProfileVO selectRole(Long userId, RoleSelectRequest req) {

        if (req.getRole() == null || (!ROLE_STUDENT.equals(req.getRole()) && !ROLE_COACH.equals(req.getRole()))) {

            throw new BusinessException(400, "身份无效");

        }

        User user = requireUser(userId);

        user.setRole(req.getRole());

        userMapper.updateById(user);

        return getProfile(userId);

    }



    public List<CoachAuthVO> listCoachAuth(Long userId) {

        requireStudent(userId);

        List<CoachAuthorization> list = coachAuthMapper.selectList(

                new LambdaQueryWrapper<CoachAuthorization>()

                        .eq(CoachAuthorization::getStudentId, userId)

                        .orderByDesc(CoachAuthorization::getCreatedAt));

        List<CoachAuthVO> result = new ArrayList<>();

        for (CoachAuthorization ca : list) {

            User coach = userMapper.selectById(ca.getCoachId());

            if (coach == null) continue;

            CoachAuthVO vo = new CoachAuthVO();

            vo.setId(ca.getId());

            vo.setCoachName(coach.getNickname() != null ? coach.getNickname() : "教练");

            vo.setCoachPhone(maskPhone(coach.getPhone()));

            vo.setCreatedAt(ca.getCreatedAt());

            result.add(vo);

        }

        return result;

    }



    @Transactional

    public CoachAuthVO addCoachAuth(Long userId, CoachAuthRequest req) {

        requireStudent(userId);

        if (req.getCoachPhone() == null || !req.getCoachPhone().matches("^1\\d{10}$")) {

            throw new BusinessException(400, "请输入正确的手机号");

        }

        User coach = userMapper.selectOne(new LambdaQueryWrapper<User>()

                .eq(User::getPhone, req.getCoachPhone())

                .eq(User::getRole, ROLE_COACH));

        if (coach == null) {

            throw new BusinessException(404, "该手机号尚未注册教练账号");

        }

        if (coach.getId().equals(userId)) {

            throw new BusinessException(400, "不能授权自己");

        }

        Long exists = coachAuthMapper.selectCount(new LambdaQueryWrapper<CoachAuthorization>()

                .eq(CoachAuthorization::getStudentId, userId)

                .eq(CoachAuthorization::getCoachId, coach.getId()));

        if (exists > 0) {

            throw new BusinessException(400, "已授权该教练");

        }

        CoachAuthorization ca = new CoachAuthorization();

        ca.setStudentId(userId);

        ca.setCoachId(coach.getId());

        ca.setCreatedAt(LocalDateTime.now());

        coachAuthMapper.insert(ca);

        CoachAuthVO vo = new CoachAuthVO();

        vo.setId(ca.getId());

        vo.setCoachName(coach.getNickname() != null ? coach.getNickname() : "教练");

        vo.setCoachPhone(maskPhone(coach.getPhone()));

        vo.setCreatedAt(ca.getCreatedAt());

        return vo;

    }



    public void revokeCoachAuth(Long userId, Long authId) {

        requireStudent(userId);

        CoachAuthorization ca = coachAuthMapper.selectById(authId);

        if (ca == null || !ca.getStudentId().equals(userId)) {

            throw new BusinessException(404, "授权记录不存在");

        }

        coachAuthMapper.deleteById(authId);

    }



    public List<String> getStrokeOrder(Long userId) {

        User user = requireUser(userId);

        try {

            return objectMapper.readValue(

                    user.getStrokeOrder() != null ? user.getStrokeOrder() : DEFAULT_STROKE_ORDER,

                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

        } catch (Exception e) {

            throw new BusinessException(500, "泳姿顺序数据异常");

        }

    }



    public List<String> updateStrokeOrder(Long userId, StrokeOrderRequest req) {

        if (req.getOrder() == null || req.getOrder().isEmpty()) {

            throw new BusinessException(400, "顺序不能为空");

        }

        User user = requireUser(userId);

        try {

            user.setStrokeOrder(objectMapper.writeValueAsString(req.getOrder()));

        } catch (Exception e) {

            throw new BusinessException(500, "保存失败");

        }

        userMapper.updateById(user);

        return req.getOrder();

    }



    private void requireStudent(Long userId) {

        User user = requireUser(userId);

        if (!ROLE_STUDENT.equals(user.getRole())) {

            throw new BusinessException(403, "仅学员可操作");

        }

    }



    private User requireUser(Long userId) {

        User user = userMapper.selectById(userId);

        if (user == null) {

            throw new BusinessException(404, "用户不存在");

        }

        return user;

    }



    private String maskPhone(String phone) {

        if (phone == null || phone.length() < 11) return phone;

        return phone.substring(0, 3) + "****" + phone.substring(7);

    }

}

