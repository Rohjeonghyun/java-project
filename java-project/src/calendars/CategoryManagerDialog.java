package calendars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * 카테고리를 추가/삭제하는 JDialog 창입니다. (ScheduleDialog로부터 호출됨)
 * 11주차 JTable/Vector 예제를 응용하여 JList와 Vector로 CRUD를 구현합니다.
 *
 */
public class CategoryManagerDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JList<String> categoryList;
    private DefaultListModel<String> listModel; // JList에 보여줄 모델
    private JTextField newCategoryField;
    private JButton addButton, deleteButton, closeButton;

    // --- 데이터 ---
    private Vector<String> categories; // ScheduleDialog로부터 받은 "원본" 카테고리 Vector

    /**
     * 카테고리 관리자 생성자
     * @param parent 부모 다이얼로그 (ScheduleDialog)
     * @param categories 현재 카테고리 목록 (원본 Vector)
     */
    public CategoryManagerDialog(Dialog parent, String title, Vector<String> categories) {
        super(parent, title, ModalityType.APPLICATION_MODAL); // 부모(ScheduleDialog) 기준 Modal
        this.categories = categories; // 원본 Vector의 참조를 저장

        // --- 1. 메인 패널 (BorderLayout) ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 2. 카테고리 목록 (CENTER) ---
        listModel = new DefaultListModel<>();
        listModel.addAll(categories); // 현재 Vector 내용으로 JList 모델 채우기
        categoryList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(categoryList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. 입력 및 추가 패널 (NORTH) ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        newCategoryField = new JTextField();
        addButton = new JButton("추가");
        
        inputPanel.add(new JLabel("새 카테고리:"), BorderLayout.WEST);
        inputPanel.add(newCategoryField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // --- 4. 삭제 및 닫기 패널 (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = new JButton("선택 삭제");
        closeButton = new JButton("닫기");
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- 5. 이벤트 리스너 연결 ---
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        closeButton.addActionListener(this);
        newCategoryField.addActionListener(this); // Enter 키로 '추가'

        // --- 6. 다이얼로그 설정 ---
        setContentPane(mainPanel);
        setSize(350, 400);
        setLocationRelativeTo(null); // 부모 다이얼로그 중앙에 표시
    }

    /**
     * 버튼 클릭 이벤트 처리
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton || source == newCategoryField) {
            // "추가" 버튼 (11주차 JTable 예제의 '추가' 기능)
            //
            String newCategory = newCategoryField.getText().trim();
            if (!newCategory.isEmpty() && !categories.contains(newCategory)) {
                // 1. 원본 Vector 수정
                categories.add(newCategory); 
                // 2. UI(JList) 모델 수정
                listModel.addElement(newCategory);
                // 3. (DB 연동 시) DB에 INSERT
                
                newCategoryField.setText("");
            }
        } else if (source == deleteButton) {
            // "선택 삭제" 버튼 (11주차 JTable 예제의 '삭제' 기능)
            //
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                // 1. 원본 Vector 수정
                categories.remove(selected);
                // 2. UI(JList) 모델 수정
                listModel.removeElement(selected);
                // 3. (DB 연동 시) DB에서 DELETE
            }
        } else if (source == closeButton) {
            // "닫기" 버튼
            dispose();
        }
    }
}