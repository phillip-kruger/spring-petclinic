package org.springframework.samples.petclinic.vet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VetResourceTests {

	@InjectMock
	VetRepository vetRepository;

	@Test
	void shouldShowVetListAsHtml() {
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		Page<Vet> page = new PageImpl<>(List.of(vet));
		Mockito.when(vetRepository.findAll(Mockito.any(Pageable.class))).thenReturn(page);

		given()
			.when().get("/vets.html")
			.then().statusCode(200)
			.body(containsString("James"))
			.body(containsString("Carter"));
	}

	@Test
	void shouldShowVetListAsJson() {
		Vet vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		Mockito.when(vetRepository.findAll()).thenReturn(List.of(vet));

		given()
			.accept("application/json")
			.when().get("/vets")
			.then().statusCode(200)
			.body("vetList[0].firstName", equalTo("James"));
	}

}
