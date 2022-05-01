package org.example.osgi.spring.boot.demo.one.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DemoOneEntity {
	
	@Id
	@EqualsAndHashCode.Include
	private Long id;
	
	private String name;

}
