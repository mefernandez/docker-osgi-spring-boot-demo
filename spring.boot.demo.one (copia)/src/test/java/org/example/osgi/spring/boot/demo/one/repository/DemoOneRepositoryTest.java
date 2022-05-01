package org.example.osgi.spring.boot.demo.one.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@TestPropertySource(properties = {
	"spring.sql.init.mode=never",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class DemoOneRepositoryTest {
	
	@Autowired
	private DemoOneRepository repository;

	@Test
	@Sql(statements = {
		"insert into demo_one_entity (id, name) values (1, 'name')"
	})
	void testCount() {
		assertEquals(1, repository.count());
	}

}
