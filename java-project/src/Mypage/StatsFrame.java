package Mypage;
//StatsFrame.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
* My Stats 화면
*  - 지금은 간단한 파이차트 자리만 그림
*  - 나중에 DB에서 카테고리별 통계 읽어서 실제 그래프를 그리면 됨.
*/
public class StatsFrame extends JFrame {

public StatsFrame(Component owner) {
   setTitle("MyStats");
   setSize(500, 500);
   setLocationRelativeTo(owner);    // 메인창 기준 중앙
   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   JPanel panel = new JPanel(new BorderLayout());
   panel.setBorder(new TitledBorder("My Stats"));
   panel.setBackground(Color.WHITE);

   //선택 콤보박스
   JPanel top = new JPanel();
   top.setBackground(Color.WHITE);
   JComboBox<String> cbMonth = new JComboBox<>(
           new String[]{"통계 항목", "할 일", "루틴", "카테고리"}
   );
   top.add(cbMonth);
   panel.add(top, BorderLayout.NORTH);

   //파이차트(더미)
   JPanel chartPanel = new JPanel() {
       @Override
       protected void paintComponent(Graphics g) {
           super.paintComponent(g);
           int size = Math.min(getWidth(), getHeight()) - 40;
           int x = (getWidth() - size) / 2;
           int y = (getHeight() - size) / 2;
           g.setColor(new Color(230, 230, 230));
           g.fillOval(x, y, size, size);
           g.setColor(Color.DARK_GRAY);
           g.drawString("Stats Pie Chart (Dummy)", x + 20, y + size / 2);

           // TODO: DB에서 카테고리별 비율을 읽어와
           //       여러 색상의 파이 조각으로 나누어 그리는 코드 추가
       }
   };
   chartPanel.setBackground(Color.WHITE);
   panel.add(chartPanel, BorderLayout.CENTER);

   // 하단: 닫기 버튼
   JPanel bottom = new JPanel();
   bottom.setBackground(Color.WHITE);
   JButton btnClose = new JButton("Close");
   btnClose.addActionListener(e -> dispose());
   bottom.add(btnClose);
   panel.add(bottom, BorderLayout.SOUTH);

   add(panel);
}
}
