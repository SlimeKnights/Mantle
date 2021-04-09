package slimeknights.mantle.client.book.data;

import java.util.ArrayList;

public class LinkedSectionData extends SectionData {

  private final transient ArrayList<SectionData> sections = new ArrayList<>();

  public LinkedSectionData() {
    super(true);
  }

  @Override
  public void load() {
    this.pages.clear();

    this.name = this.name.toLowerCase();

    for (SectionData section : this.sections) {
      section.parent = this.parent;
      section.unnamedPageCounter = this.unnamedPageCounter;
      section.load();
      this.unnamedPageCounter = section.unnamedPageCounter;

      this.pages.addAll(section.pages);
    }

    this.icon.load(this.source);
  }

  public void addSection(SectionData data) {
    if (!data.name.equalsIgnoreCase(this.name) && !this.sections.isEmpty()) {
      throw new IllegalArgumentException("Linked sections must contain all sections of the same name.");
    }

    if (this.sections.isEmpty()) {
      this.name = data.name;
      this.icon = data.icon;
      this.hideWhenLocked = data.hideWhenLocked;
    }

    this.requirements.addAll(data.requirements);
    this.sections.add(data);
  }
}
