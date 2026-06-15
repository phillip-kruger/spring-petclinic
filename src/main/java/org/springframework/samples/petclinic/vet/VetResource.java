package org.springframework.samples.petclinic.vet;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Path("/")
public class VetResource {

	@Inject
	VetRepository vetRepository;

	@Inject
	@Location("vets/vetList")
	Template vetList;

	@GET
	@Path("/vets.html")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showVetList(@RestQuery @DefaultValue("1") int page) {
		Page<Vet> paginated = findPaginated(page);
		List<Vet> listVets = paginated.getContent();
		return vetList.data("currentPage", page)
				.data("totalPages", paginated.getTotalPages())
				.data("totalItems", paginated.getTotalElements())
				.data("listVets", listVets);
	}

	@GET
	@Path("/vets")
	@Produces(MediaType.APPLICATION_JSON)
	public Vets showResourcesVetList() {
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		return vetRepository.findAll(PageRequest.of(page - 1, pageSize));
	}

}
