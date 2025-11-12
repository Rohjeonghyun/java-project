package calendars;

import javax.swing.*;
import java.awt.*;

public class CalendarPanel extends JPanel {
    public CalendarPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("CalendarPanel 화면"), BorderLayout.NORTH);
        
    }
}
