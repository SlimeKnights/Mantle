package slimeknights.mantle.client.book.data;

import java.util.ArrayList;

public class LinkedSectionData extends SectionData {
    private transient ArrayList<SectionData> sections = new ArrayList<>();

    public LinkedSectionData(){
        super(true);
    }

    @Override
    public void load() {
        pages.clear();

        name = name.toLowerCase();

        for(SectionData section : sections){
            section.parent = parent;
            section.unnamedPageCounter = unnamedPageCounter;
            section.load();
            unnamedPageCounter = section.unnamedPageCounter;

            pages.addAll(section.pages);
        }

        icon.location = source.getResourceLocation(icon.file, true);
    }

    public void addSection(SectionData data){
        if(!data.name.equalsIgnoreCase(name) && !sections.isEmpty()){
            throw new IllegalArgumentException("Linked sections must contain all sections of the same name.");
        }

        if(sections.isEmpty()){
            name = data.name;
            icon = data.icon;
            hideWhenLocked = data.hideWhenLocked;

        }

        requirements.addAll(data.requirements);
        sections.add(data);
    }
}
