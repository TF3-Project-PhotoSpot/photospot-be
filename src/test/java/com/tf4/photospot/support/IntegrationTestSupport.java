package com.tf4.photospot.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import com.tf4.photospot.mockobject.MockS3Config;

@Transactional
@SpringBootTest
@Import(MockS3Config.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class IntegrationTestSupport {
}
