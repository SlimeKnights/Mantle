package slimeknights.mantle.client.book.repository;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.SectionDataModule;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.pulsar.control.PulseManager;

public class ModuleFileRepository extends FileRepository {

    private Predicate<String> manager;
	public ModuleFileRepository(PulseManager manager, String location) {
		super(location);
		this.manager = manager::isPulseLoaded;
	}

    public ModuleFileRepository(String location) {
        super(location);
        this.manager = PulseManager::isPulseLoadedGlobal;
    }

	@Override
	public List<SectionData> getSections() {
		// same as super, except we remove any where the related module is not loaded
		return Arrays.stream(BookLoader.GSON.fromJson(this.resourceToString(this.getResource(this.getResourceLocation("index.json"))), SectionDataModule[].class))
				.filter((section)->section.module.isEmpty() || this.manager.test(section.module))
				.collect(Collectors.toList());
	}

	/**
	 * Gets the manager used to determine if a module is available
	 * @return  Predicate to test a module availability
	 */
	public Predicate<String> getManager() {
		return this.manager;
	}
}
