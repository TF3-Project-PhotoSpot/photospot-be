package com.tf4.photospot.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.tf4.photospot.global.exception.ApiException;
import com.tf4.photospot.global.exception.domain.CommonErrorCode;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE - 1) // @Transaction 전 순서, 다른 aop가 추가 된다면 Order에 대한 고려
public class RetryAspect {
	@Around("@annotation(retry)")
	public Object retry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
		for (int retryCount = 0; retryCount <= retry.maxRetryCount(); retryCount++) {
			try {
				return joinPoint.proceed();
			} catch (CannotAcquireLockException | OptimisticLockException | OptimisticLockingFailureException ex) {
				log.error("[RETRY FAILED][{}/{}] {}", retryCount, retry.maxRetryCount(), ex.getClass().getName());
			}
			Thread.sleep(retry.delayForMillis()); // delay 방식은 상황에 따라 바뀔 수 있음
		}
		throw new ApiException(CommonErrorCode.FAILED_BECAUSE_OF_CONCURRENCY_UPDATE);
	}
}
