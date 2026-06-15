package org.springframework.samples.petclinic.owner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class OwnerResourceTests {

	@InjectMock
	OwnerRepository owners;

	@Test
	void shouldShowOwnerCreationForm() {
		given()
			.when().get("/owners/new")
			.then().statusCode(200)
			.body(containsString("Owner"));
	}

	@Test
	void shouldCreateOwner() {
		Mockito.when(owners.save(Mockito.any(Owner.class))).thenAnswer(invocation -> {
			Owner o = invocation.getArgument(0);
			o.setId(1);
			return o;
		});

		given()
			.redirects().follow(false)
			.contentType("application/x-www-form-urlencoded")
			.formParam("firstName", "Joe")
			.formParam("lastName", "Bloggs")
			.formParam("address", "123 Caramel Street")
			.formParam("city", "London")
			.formParam("telephone", "1234567890")
			.when().post("/owners/new")
			.then().statusCode(303)
			.header("Location", containsString("/owners/"));
	}

	@Test
	void shouldRejectInvalidOwner() {
		given()
			.contentType("application/x-www-form-urlencoded")
			.formParam("firstName", "")
			.formParam("lastName", "")
			.formParam("address", "")
			.formParam("city", "")
			.formParam("telephone", "")
			.when().post("/owners/new")
			.then().statusCode(200)
			.body(containsString("Owner"));
	}

	@Test
	void shouldShowFindOwnersForm() {
		given()
			.when().get("/owners/find")
			.then().statusCode(200)
			.body(containsString("Find Owners"));
	}

	@Test
	void shouldListOwners() {
		Owner owner = createTestOwner(1, "George", "Franklin");
		Page<Owner> page = new PageImpl<>(List.of(owner, createTestOwner(2, "Betty", "Davis")));
		Mockito.when(owners.findByLastNameStartingWith(Mockito.anyString(), Mockito.any(Pageable.class)))
				.thenReturn(page);

		given()
			.queryParam("lastName", "")
			.when().get("/owners")
			.then().statusCode(200)
			.body(containsString("George"));
	}

	@Test
	void shouldRedirectWhenSingleOwner() {
		Owner owner = createTestOwner(1, "George", "Franklin");
		Page<Owner> page = new PageImpl<>(List.of(owner));
		Mockito.when(owners.findByLastNameStartingWith(Mockito.anyString(), Mockito.any(Pageable.class)))
				.thenReturn(page);

		given()
			.redirects().follow(false)
			.queryParam("lastName", "Franklin")
			.when().get("/owners")
			.then().statusCode(303)
			.header("Location", containsString("/owners/1"));
	}

	@Test
	void shouldShowOwnerDetails() {
		Owner owner = createTestOwner(1, "George", "Franklin");
		Mockito.when(owners.findById(1)).thenReturn(Optional.of(owner));

		given()
			.when().get("/owners/1")
			.then().statusCode(200)
			.body(containsString("George"))
			.body(containsString("Franklin"));
	}

	private Owner createTestOwner(int id, String firstName, String lastName) {
		Owner owner = new Owner();
		owner.setId(id);
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");
		return owner;
	}

}
