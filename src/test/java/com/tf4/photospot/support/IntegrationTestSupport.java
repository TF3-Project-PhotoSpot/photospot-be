package com.tf4.photospot.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Transactional
@SpringBootTest
public abstract class IntegrationTestSupport {
	@Autowired
	protected EntityManager em;
}
