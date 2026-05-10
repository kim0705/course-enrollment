package com.yujin.course_enrollment.service;

import com.yujin.course_enrollment.dto.req.ReqEnrollmentCreateDto;
import com.yujin.course_enrollment.dto.req.ReqEnrollmentPageDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentDto;
import com.yujin.course_enrollment.dto.resp.RespEnrollmentStudentDto;
import com.yujin.course_enrollment.dto.resp.RespPageDto;
import com.yujin.course_enrollment.entity.Course;
import com.yujin.course_enrollment.entity.Enrollment;
import com.yujin.course_enrollment.entity.User;
import com.yujin.course_enrollment.global.CourseStatus;
import com.yujin.course_enrollment.global.EnrollmentStatus;
import com.yujin.course_enrollment.global.exception.BusinessException;
import com.yujin.course_enrollment.mapper.CourseMapper;
import com.yujin.course_enrollment.mapper.EnrollmentMapper;
import com.yujin.course_enrollment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 수강 신청 서비스
 * 수강 신청 등록 등 수강 신청 관련 비즈니스 로직을 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final PaymentService paymentService;

    /**
     * 수강 신청
     * 정원이 남아있으면 PENDING, 초과 시 WAITLIST로 등록
     * @param userId 신청하는 사용자 ID
     * @param reqEnrollmentCreateDto 수강 신청 요청 DTO
     * @throws BusinessException 사용자 없음(400), 강의 없음(400), 본인 강의 신청(403), 모집 중 아님(400), 중복 신청(400)
     */
    @Transactional
    public RespEnrollmentDto registerEnrollment(Long userId, ReqEnrollmentCreateDto reqEnrollmentCreateDto) {
        log.info("[EnrollmentService] 수강 신청 - userId: {}, courseId: {}", userId, reqEnrollmentCreateDto.getCourseId());

        // 사용자 존재 여부 확인
        User user = userMapper.selectUserById(userId);
        if (user == null) {
            log.warn("[EnrollmentService] 존재하지 않는 사용자 - userId: {}", userId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        Long courseId = reqEnrollmentCreateDto.getCourseId();

        // 강의 존재 여부 확인
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            log.warn("[EnrollmentService] 강의 없음 - courseId: {}", courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 강의입니다.");
        }

        // 본인 강의 신청 불가
        if (course.getCreatorId().equals(userId)) {
            log.warn("[EnrollmentService] 본인 강의 신청 시도 - userId: {}, courseId: {}", userId, courseId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인이 개설한 강의는 신청할 수 없습니다.");
        }

        // 강의 모집 중 확인
        if (!CourseStatus.OPEN.equals(course.getStatus())) {
            log.warn("[EnrollmentService] 모집 중이 아님 - courseId: {}, status: {}", courseId, course.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "모집 중인 강의만 신청할 수 있습니다.");
        }

        // 중복 신청 확인 (CANCELLED 상태만 재신청 허용)
        Enrollment existing = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);
        if (existing != null && !EnrollmentStatus.CANCELLED.equals(existing.getStatus())) {

            if (EnrollmentStatus.WAITLIST.equals(existing.getStatus())) {
                log.warn("[EnrollmentService] 이미 대기 중 - userId: {}, courseId: {}", userId, courseId);
                throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 대기 중인 강의입니다.");
            }

            log.warn("[EnrollmentService] 중복 신청 - userId: {}, courseId: {}", userId, courseId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 신청한 강의입니다.");
        }

        // 정원 초과 시 WAITLIST 등록
        if (course.getEnrolledCount() >= course.getCapacity()) {
            log.info("[EnrollmentService] 정원 초과 - 대기열 등록 - userId: {}, courseId: {}", userId, courseId);

            enrollmentMapper.insertEnrollment(Enrollment.ofWaitlist(userId, courseId));
            Enrollment saved = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);

            return RespEnrollmentDto.of(saved, course.getTitle());
        }

        // 자리 확보 먼저 → 성공하면 PENDING, 실패하면 WAITLIST
        int updated = courseMapper.updateCourseEnrolledCountPlus(courseId);
        if (updated == 0) {
            log.info("[EnrollmentService] 동시 신청으로 정원 초과 - 대기열 등록 - userId: {}, courseId: {}", userId, courseId);

            enrollmentMapper.insertEnrollment(Enrollment.ofWaitlist(userId, courseId));
            Enrollment saved = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);

            return RespEnrollmentDto.of(saved, course.getTitle());
        }

        enrollmentMapper.insertEnrollment(reqEnrollmentCreateDto.toEntity(userId));

        log.info("[EnrollmentService] 수강 신청 완료 - userId: {}, courseId: {}", userId, courseId);

        Enrollment saved = enrollmentMapper.selectEnrollmentByUserIdAndCourseId(userId, courseId);

        return RespEnrollmentDto.of(saved, course.getTitle());
    }

    /**
     * 나의 수강 신청 목록 조회
     * @param userId 사용자 ID
     * @param reqEnrollmentPageDto 페이징 조건 DTO
     */
    public RespPageDto<RespEnrollmentStudentDto> findMyEnrollments(Long userId, ReqEnrollmentPageDto reqEnrollmentPageDto) {
        log.info("[EnrollmentService] 나의 수강 신청 목록 조회 - userId: {}, page: {}, size: {}", userId, reqEnrollmentPageDto.getPage(), reqEnrollmentPageDto.getSize());

        reqEnrollmentPageDto.setUserId(userId);

        List<RespEnrollmentStudentDto> content = enrollmentMapper.selectEnrollmentListByUserId(reqEnrollmentPageDto);

        int totalCount = enrollmentMapper.selectEnrollmentListByUserIdCount(userId);

        return RespPageDto.of(content, reqEnrollmentPageDto.getPage(), reqEnrollmentPageDto.getSize(), totalCount);
    }

    /**
     * 결제 요청 (PENDING → CONFIRMED)
     * @param userId 사용자 ID
     * @param enrollmentId 수강 신청 ID
     * @throws BusinessException 수강 신청 없음(400), 본인 신청 아님(403), PENDING 상태 아님(400)
     */
    @Transactional
    public RespEnrollmentDto confirmEnrollment(Long userId, Long enrollmentId) {
        log.info("[EnrollmentService] 결제 요청 - userId: {}, enrollmentId: {}", userId, enrollmentId);

        // 수강 신청 존재 여부 확인
        Enrollment enrollment = enrollmentMapper.selectEnrollmentById(enrollmentId);
        if (enrollment == null) {
            log.warn("[EnrollmentService] 수강 신청 없음 - enrollmentId: {}", enrollmentId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 수강 신청입니다.");
        }

        // 본인 신청 확인
        if (!enrollment.getUserId().equals(userId)) {
            log.warn("[EnrollmentService] 본인 신청 아님 - userId: {}, enrollmentId: {}", userId, enrollmentId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 수강 신청만 결제할 수 있습니다.");
        }

        // PENDING 상태 확인
        if (!EnrollmentStatus.PENDING.equals(enrollment.getStatus())) {
            log.warn("[EnrollmentService] PENDING 상태 아님 - enrollmentId: {}, status: {}", enrollmentId, enrollment.getStatus());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PENDING 상태의 수강 신청만 결제할 수 있습니다.");
        }

        enrollmentMapper.updateEnrollmentStatus(Enrollment.ofConfirm(enrollmentId));

        log.info("[EnrollmentService] 결제 요청 완료 - enrollmentId: {}", enrollmentId);

        Course course = courseMapper.selectCourseById(enrollment.getCourseId());

        Enrollment saved = enrollmentMapper.selectEnrollmentById(enrollmentId);

        return RespEnrollmentDto.of(saved, course.getTitle());
    }

    /**
     * 수강 취소 (PENDING, CONFIRMED, WAITLIST → CANCELLED)
     * CONFIRMED 유료 강의 취소 시 토스 환불 API 호출 후 상태 변경
     * PENDING/CONFIRMED 취소 시 대기열 첫 번째 사람 자동 PENDING 승격
     * @param userId 사용자 ID
     * @param enrollmentId 수강 신청 ID
     * @param cancelReason 취소 사유 (CONFIRMED 취소 시 필수)
     * @throws BusinessException 수강 신청 없음(400), 본인 신청 아님(403), 이미 취소됨(400), CONFIRMED 7일 초과(400), 취소 사유 없음(400)
     */
    @Transactional
    public RespEnrollmentDto cancelEnrollment(Long userId, Long enrollmentId, String cancelReason) {
        log.info("[EnrollmentService] 수강 취소 - userId: {}, enrollmentId: {}", userId, enrollmentId);

        // 수강 신청 존재 여부 확인
        Enrollment enrollment = enrollmentMapper.selectEnrollmentById(enrollmentId);
        if (enrollment == null) {
            log.warn("[EnrollmentService] 수강 신청 없음 - enrollmentId: {}", enrollmentId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "존재하지 않는 수강 신청입니다.");
        }

        // 본인 신청 확인
        if (!enrollment.getUserId().equals(userId)) {
            log.warn("[EnrollmentService] 본인 신청 아님 - userId: {}, enrollmentId: {}", userId, enrollmentId);
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인의 수강 신청만 취소할 수 있습니다.");
        }

        // 이미 취소됨 확인
        if (EnrollmentStatus.CANCELLED.equals(enrollment.getStatus())) {
            log.warn("[EnrollmentService] 이미 취소됨 - enrollmentId: {}", enrollmentId);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 취소된 수강 신청입니다.");
        }

        // CONFIRMED 상태 취소 기간 확인 (확정 후 7일 이내) + 환불 처리
        if (EnrollmentStatus.CONFIRMED.equals(enrollment.getStatus())) {
            if (enrollment.getConfirmedAt().plusDays(7).isBefore(LocalDateTime.now())) {
                log.warn("[EnrollmentService] 취소 기간 초과 - enrollmentId: {}, confirmedAt: {}", enrollmentId, enrollment.getConfirmedAt());
                throw new BusinessException(HttpStatus.BAD_REQUEST, "수강 확정 후 7일이 지나 취소할 수 없습니다.");
            }

            if (cancelReason == null || cancelReason.isBlank()) {
                log.warn("[EnrollmentService] 취소 사유 없음 - enrollmentId: {}", enrollmentId);
                throw new BusinessException(HttpStatus.BAD_REQUEST, "취소 사유를 입력해주세요.");
            }

            // 토스 환불 API 호출 후 payment CANCELLED 업데이트 (외부 API 먼저 호출)
            paymentService.refund(enrollmentId, cancelReason);
        }

        enrollmentMapper.updateEnrollmentStatus(Enrollment.ofCancel(enrollmentId));

        // WAITLIST 취소는 enrolled_count 변경 및 승격 없음
        if (EnrollmentStatus.WAITLIST.equals(enrollment.getStatus())) {
            log.info("[EnrollmentService] 대기 취소 완료 - enrollmentId: {}", enrollmentId);

            Course course = courseMapper.selectCourseById(enrollment.getCourseId());
            Enrollment saved = enrollmentMapper.selectEnrollmentById(enrollmentId);

            return RespEnrollmentDto.of(saved, course.getTitle());
        }

        courseMapper.updateCourseEnrolledCountMinus(enrollment.getCourseId());

        // 대기열 승격 시도 (동시 경쟁 실패 시 다음 대기자로 재시도)
        Enrollment nextWaitlist;
        while ((nextWaitlist = enrollmentMapper.selectNextWaitlist(enrollment.getCourseId())) != null) {
            int promoted = enrollmentMapper.updateEnrollmentStatusPromote(nextWaitlist.getId());

            if (promoted > 0) {
                courseMapper.updateCourseEnrolledCountPlus(enrollment.getCourseId());
                log.info("[EnrollmentService] 대기열 승격 - enrollmentId: {}", nextWaitlist.getId());

                break;
            }
        }

        log.info("[EnrollmentService] 수강 취소 완료 - enrollmentId: {}", enrollmentId);

        Course course = courseMapper.selectCourseById(enrollment.getCourseId());
        Enrollment saved = enrollmentMapper.selectEnrollmentById(enrollmentId);

        return RespEnrollmentDto.of(saved, course.getTitle());
    }
}
