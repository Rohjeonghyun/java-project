package mypage;

import javax.swing.*;
import java.awt.*;

public class MypagePanel extends JPanel {
    public MypagePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("여기에 MyPAGE 화면", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}

