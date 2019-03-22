package slimeknights.mantle.client.book.repository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.SectionDataModule;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.pulsar.control.PulseManager;

public class ModuleFileRepository extends FileRepository {

    private PulseManager manager;
	public ModuleFileRepository(PulseManager manager, String location) {
		super(location);
		this.manager = manager;
	}

	@Override
	public List<SectionData> getSections() {
		// same as super, except we remove any where the related module is not loaded
		return Arrays.stream(BookLoader.GSON.fromJson(resourceToString(getResource(getResourceLocation("index.json"))), SectionDataModule[].class))
				.filter((section)->section.module.isEmpty() || manager.isPulseLoaded(section.module))
				.collect(Collectors.toList());
	}
}
