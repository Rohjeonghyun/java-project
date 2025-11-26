package todo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TodoDetailDialog extends JDialog {

    private final TodoItem item;            // 원본 객체
    private final JList<TodoItem> ownerList; // repaint용 (화면 갱신)

    private JTextField txtTitle;
    private JCheckBox chkDone;
    private JLabel lblOver;
    private JLabel lbltime;
    private JLabel lblEndTime;

    public TodoDetailDialog(Window owner, TodoItem item, JList<TodoItem> ownerList) {
        super(owner, "할 일 상세보기", ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.ownerList = ownerList;

        setLayout(new BorderLayout(10, 10));

        // 상단 제목
        JLabel header = new JLabel("할 일 상세 정보", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        add(header, BorderLayout.NORTH);

        // 중앙
        JPanel centerPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 제목
        centerPanel.add(new JLabel("할 일:"));
        txtTitle = new JTextField(item.text); // TodoItem의 text 사용
        centerPanel.add(txtTitle);
        txtTitle.setEditable(false);

        // 완료 여부
        centerPanel.add(new JLabel("완료 여부:"));
        chkDone = new JCheckBox("완료", item.done);
        centerPanel.add(chkDone);
        
        //시간
        centerPanel.add(new JLabel("시작 시간:"));
        String timeText = (item.time != null && !item.time.isEmpty())
                ? item.time : "-";
        lbltime = new JLabel(timeText);
        centerPanel.add(lbltime);
        
        
        // 끝시간
        centerPanel.add(new JLabel("끝 시간:"));
        String endText = (item.endTime != null && !item.endTime.isEmpty())
                ? item.endTime : "-";
        lblEndTime = new JLabel(endText);
        centerPanel.add(lblEndTime);

        
        
        
        // 밀린 할 일 여부(over)
        centerPanel.add(new JLabel("상태:"));
        String overText = item.over ? "밀린 할 일" : "오늘 할 일";
        lblOver = new JLabel(overText);
        centerPanel.add(lblOver);

        add(centerPanel, BorderLayout.CENTER);

        // 하단 버튼 저장
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("저장");
        JButton btnClose = new JButton("닫기");

        bottomPanel.add(btnSave);
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        // 버튼 이벤트
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyChanges();
            }
        });

        btnClose.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    
    private void applyChanges() {
        
        item.done = chkDone.isSelected();

        if (ownerList != null) {
            ownerList.repaint(); //  다시 그리기
        }
        dispose();
    }
}
