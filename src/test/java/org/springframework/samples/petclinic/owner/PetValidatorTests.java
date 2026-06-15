package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class PetValidatorTests {

	private final PetValidator validator = new PetValidator();

	@Test
	void shouldValidateValidPet() {
		Pet pet = new Pet();
		pet.setName("Buddy");
		pet.setBirthDate(LocalDate.of(2020, 1, 1));
		PetType type = new PetType();
		type.setName("dog");
		pet.setType(type);

		List<String> errors = validator.validate(pet);
		assertThat(errors).isEmpty();
	}

	@Test
	void shouldRejectEmptyName() {
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.of(2020, 1, 1));
		PetType type = new PetType();
		type.setName("dog");
		pet.setType(type);

		List<String> errors = validator.validate(pet);
		assertThat(errors).isNotEmpty();
	}

	@Test
	void shouldRejectMissingBirthDate() {
		Pet pet = new Pet();
		pet.setName("Buddy");
		PetType type = new PetType();
		type.setName("dog");
		pet.setType(type);

		List<String> errors = validator.validate(pet);
		assertThat(errors).isNotEmpty();
	}

	@Test
	void shouldRejectMissingTypeForNewPet() {
		Pet pet = new Pet();
		pet.setName("Buddy");
		pet.setBirthDate(LocalDate.of(2020, 1, 1));

		List<String> errors = validator.validate(pet);
		assertThat(errors).isNotEmpty();
	}

}
