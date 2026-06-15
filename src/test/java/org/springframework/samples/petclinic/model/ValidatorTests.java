package org.springframework.samples.petclinic.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.owner.Owner;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@QuarkusTest
class ValidatorTests {

	@Inject
	Validator validator;

	@Test
	void shouldNotValidateWhenFirstNameEmpty() {
		Owner owner = new Owner();
		owner.setFirstName("");
		owner.setLastName("Doe");
		owner.setAddress("123 Main St");
		owner.setCity("Springfield");
		owner.setTelephone("1234567890");

		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).isNotEmpty();
	}

}
