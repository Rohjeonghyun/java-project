package todo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoutineDetailDialog extends JDialog {

    private final JTextField txtStartTime;
    private boolean confirmed = false;   // 확인 눌렀는지 여부

    public RoutineDetailDialog(Window owner, String routineTitle, String currentStart) {
        super(owner, "루틴 상세 설정", ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new GridLayout(2, 2, 5, 5));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 루틴 제목 (읽기 전용 라벨)
        content.add(new JLabel("루틴 이름:"));
        JLabel lblTitle = new JLabel(routineTitle);
        content.add(lblTitle);

        // 시작 시간 입력
        content.add(new JLabel("시작 시간 (예: 08:00):"));
        txtStartTime = new JTextField();
        if (currentStart != null) {
            txtStartTime.setText(currentStart);
        }
        content.add(txtStartTime);

        add(content, BorderLayout.CENTER);

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        JButton btnOk = new JButton("확인");
        JButton btnCancel = new JButton("취소");

        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // 버튼 이벤트
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(owner); // 가운데 정렬
    }

    // 확인 버튼 눌렀는지 여부
    public boolean isConfirmed() {
        return confirmed;
    }

    // 사용자가 입력한 시작 시간
    public String getStartTime() {
        return txtStartTime.getText().trim();
    }
}
