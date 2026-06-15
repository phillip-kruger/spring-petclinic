package org.springframework.samples.petclinic.owner;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@Path("/owners/{ownerId}/pets")
public class PetResource {

	@Inject
	OwnerRepository owners;

	@Inject
	PetTypeRepository types;

	@Inject
	@Location("pets/createOrUpdatePetForm")
	Template petForm;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm(@PathParam("ownerId") int ownerId) {
		Owner owner = findOwner(ownerId);
		return petForm.data("pet", new Pet(), "owner", owner,
				"types", types.findPetTypes(), "errors", Map.of(), "isNew", true);
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@PathParam("ownerId") int ownerId,
			@RestForm String name,
			@RestForm String birthDate,
			@RestForm Integer type) {

		Owner owner = findOwner(ownerId);
		Pet pet = new Pet();
		Map<String, String> errors = new LinkedHashMap<>();

		populatePet(pet, name, birthDate, type, errors);

		if (name != null && !name.isBlank() && owner.getPet(name, true) != null) {
			errors.put("name", "already exists");
		}

		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
			errors.put("birthDate", "must be in the past or today");
		}

		if (!errors.isEmpty()) {
			return petForm.data("pet", pet, "owner", owner,
					"types", types.findPetTypes(), "errors", errors, "isNew", true);
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateForm(@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}
		return petForm.data("pet", pet, "owner", owner,
				"types", types.findPetTypes(), "errors", Map.of(), "isNew", false);
	}

	@POST
	@Path("/{petId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@RestForm String name,
			@RestForm String birthDate,
			@RestForm Integer type) {

		Owner owner = findOwner(ownerId);
		Pet existingPet = owner.getPet(petId);
		if (existingPet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		Map<String, String> errors = new LinkedHashMap<>();
		populatePet(existingPet, name, birthDate, type, errors);

		if (name != null && !name.isBlank()) {
			Pet duplicate = owner.getPet(name, false);
			if (duplicate != null && !Objects.equals(duplicate.getId(), petId)) {
				errors.put("name", "already exists");
			}
		}

		if (existingPet.getBirthDate() != null && existingPet.getBirthDate().isAfter(LocalDate.now())) {
			errors.put("birthDate", "must be in the past or today");
		}

		if (!errors.isEmpty()) {
			return petForm.data("pet", existingPet, "owner", owner,
					"types", types.findPetTypes(), "errors", errors, "isNew", false);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner findOwner(int ownerId) {
		return owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
	}

	private void populatePet(Pet pet, String name, String birthDate, Integer typeId, Map<String, String> errors) {
		if (name == null || name.isBlank()) {
			errors.put("name", "is required");
		} else {
			pet.setName(name);
		}

		if (birthDate == null || birthDate.isBlank()) {
			errors.put("birthDate", "is required");
		} else {
			try {
				pet.setBirthDate(LocalDate.parse(birthDate));
			} catch (Exception e) {
				errors.put("birthDate", "invalid date");
			}
		}

		if (typeId == null) {
			if (pet.isNew()) {
				errors.put("type", "is required");
			}
		} else {
			types.findById(typeId).ifPresent(pet::setType);
		}
	}

}
