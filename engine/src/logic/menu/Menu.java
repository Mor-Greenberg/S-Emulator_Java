package logic.menu;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public List<String> menuCommands;
    public Menu(){

        this.menuCommands = menuCommands();
    }
    List<String> menuCommands(){
        List<String> menuCommands  = new ArrayList<String>();
        menuCommands.add("Load XML");
        menuCommands.add("Show Program");
        menuCommands.add("Expand");
        menuCommands.add("Run Program");
        menuCommands.add("Show history/statistics");
        menuCommands.add("Exit Program");
        return menuCommands;
    }
}
