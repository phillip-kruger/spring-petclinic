package org.springframework.samples.petclinic.owner;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Path("/owners")
public class OwnerResource {

	@Inject
	OwnerRepository owners;

	@Inject
	Validator validator;

	@Inject
	@Location("owners/createOrUpdateOwnerForm")
	Template ownerForm;

	@Inject
	@Location("owners/findOwners")
	Template findOwnersTemplate;

	@Inject
	@Location("owners/ownerDetails")
	Template ownerDetails;

	@Inject
	@Location("owners/ownersList")
	Template ownersList;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		return ownerForm.data("owner", new Owner(), "errors", Map.of(), "isNew", true);
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@RestForm String firstName,
			@RestForm String lastName,
			@RestForm String address,
			@RestForm String city,
			@RestForm String telephone) {

		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		Map<String, String> errors = validate(owner);
		if (!errors.isEmpty()) {
			return ownerForm.data("owner", owner, "errors", errors, "isNew", true);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return findOwnersTemplate.data("owner", new Owner());
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Object processFindForm(@RestQuery @DefaultValue("1") int page,
			@RestQuery @DefaultValue("") String lastName) {
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);

		if (ownersResults.isEmpty()) {
			return findOwnersTemplate.data("owner", new Owner(), "notFound", true);
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner owner = ownersResults.iterator().next();
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		List<Owner> listOwners = ownersResults.getContent();
		return ownersList.data("currentPage", page)
				.data("totalPages", ownersResults.getTotalPages())
				.data("totalItems", ownersResults.getTotalElements())
				.data("listOwners", listOwners);
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		return owners.findByLastNameStartingWith(lastname, PageRequest.of(page - 1, pageSize));
	}

	@GET
	@Path("/{ownerId}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showOwner(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return ownerDetails.data("owner", owner);
	}

	@GET
	@Path("/{ownerId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return ownerForm.data("owner", owner, "errors", Map.of(), "isNew", false);
	}

	@POST
	@Path("/{ownerId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateOwnerForm(
			@PathParam("ownerId") int ownerId,
			@RestForm String firstName,
			@RestForm String lastName,
			@RestForm String address,
			@RestForm String city,
			@RestForm String telephone) {

		Owner owner = this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		Map<String, String> errors = validate(owner);
		if (!errors.isEmpty()) {
			return ownerForm.data("owner", owner, "errors", errors, "isNew", false);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Map<String, String> validate(Owner owner) {
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		return violations.stream()
				.collect(Collectors.toMap(
						v -> v.getPropertyPath().toString(),
						ConstraintViolation::getMessage,
						(a, b) -> a));
	}

}
