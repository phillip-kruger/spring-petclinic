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
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/owners/{ownerId}/pets/{petId}/visits")
public class VisitResource {

	@Inject
	OwnerRepository owners;

	@Inject
	@Location("pets/createOrUpdateVisitForm")
	Template visitForm;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initNewVisitForm(@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = findPet(owner, petId);
		Visit visit = new Visit();
		return visitForm.data("visit", visit, "pet", pet, "owner", owner,
				"errors", Map.of(), "minVisitDate", LocalDate.now().plusDays(1));
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@RestForm String date,
			@RestForm String description) {

		Owner owner = findOwner(ownerId);
		Pet pet = findPet(owner, petId);
		Visit visit = new Visit();
		Map<String, String> errors = new LinkedHashMap<>();

		if (date == null || date.isBlank()) {
			errors.put("date", "is required");
		} else {
			try {
				LocalDate visitDate = LocalDate.parse(date);
				if (!visitDate.isAfter(LocalDate.now())) {
					errors.put("date", "must be a future date");
				}
				visit.setDate(visitDate);
			} catch (Exception e) {
				errors.put("date", "invalid date");
			}
		}

		if (description == null || description.isBlank()) {
			errors.put("description", "must not be blank");
		} else {
			visit.setDescription(description);
		}

		if (!errors.isEmpty()) {
			return visitForm.data("visit", visit, "pet", pet, "owner", owner,
					"errors", errors, "minVisitDate", LocalDate.now().plusDays(1));
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner findOwner(int ownerId) {
		return owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
	}

	private Pet findPet(Owner owner, int petId) {
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}
		return pet;
	}

}
