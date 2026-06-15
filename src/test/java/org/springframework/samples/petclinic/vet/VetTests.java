package org.springframework.samples.petclinic.vet;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 */
class VetTests {

	@Test
	void serialization() throws Exception {
		Vet vet = new Vet();
		vet.setFirstName("Zaphod");
		vet.setLastName("Beeblebrox");
		vet.setId(123);

		// Serialize
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(vet);
		}

		// Deserialize
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		Vet other;
		try (ObjectInputStream in = new ObjectInputStream(bis)) {
			other = (Vet) in.readObject();
		}

		assertThat(other.getFirstName()).isEqualTo(vet.getFirstName());
		assertThat(other.getLastName()).isEqualTo(vet.getLastName());
		assertThat(other.getId()).isEqualTo(vet.getId());
	}

}
