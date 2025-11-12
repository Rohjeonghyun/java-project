package todo;

import javax.swing.*;
import java.awt.*;

public class TodoPanel extends JPanel {
    public TodoPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("여기에 Todo 화면"), BorderLayout.NORTH);
      
    }
}
