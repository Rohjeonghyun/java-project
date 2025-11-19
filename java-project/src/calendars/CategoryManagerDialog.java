package calendars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * 카테고리를 추가/삭제하는 JDialog 창입니다. (ScheduleDialog로부터 호출됨)
 * 11주차 JTable/Vector 예제를 응용하여 JList와 Vector로 CRUD를 구현합니다.
 *
 */
public class CategoryManagerDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JList<CategoryItem> categoryList;
    private DefaultListModel<CategoryItem> listModel; // JList에 보여줄 모델
    private JTextField newCategoryField;
    private JButton addButton, deleteButton, closeButton, colorButton;

    // --- 데이터 ---
    private Vector<CategoryItem> categories; // ScheduleDialog로부터 받은 "원본" 카테고리 Vector

    /**
     * 카테고리 관리자 생성자
     * @param parent 부모 다이얼로그 (ScheduleDialog)
     * @param categories 현재 카테고리 목록 (원본 Vector)
     */
    public CategoryManagerDialog(Dialog parent, String title, Vector<CategoryItem> categories) {
        super(parent, title, ModalityType.APPLICATION_MODAL); // 부모(ScheduleDialog) 기준 Modal
        this.categories = categories; // 원본 Vector의 참조를 저장

        // --- 1. 메인 패널 (BorderLayout) ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 2. 카테고리 목록 (CENTER) ---
        listModel = new DefaultListModel<>();
        listModel.addAll(categories); // 현재 Vector 내용으로 JList 모델 채우기
        categoryList = new JList<>(listModel);
        categoryList.setCellRenderer(new CategoryRenderer());
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
        colorButton = new JButton("색상 변경");
        deleteButton = new JButton("선택 삭제");
        closeButton = new JButton("닫기");
        
        buttonPanel.add(colorButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- 5. 이벤트 리스너 연결 ---
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        closeButton.addActionListener(this);
        newCategoryField.addActionListener(this); // Enter 키로 '추가'
        colorButton.addActionListener(this);

        // --- 6. 다이얼로그 설정 ---
        setContentPane(mainPanel);
        setSize(400, 450);
        setLocationRelativeTo(null); // 부모 다이얼로그 중앙에 표시
    }

    /**
     * 버튼 클릭 이벤트 처리
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton || source == newCategoryField) {
            // [추가]
            String newName = newCategoryField.getText().trim();
            if (!newName.isEmpty()) {
                // 중복 체크
                boolean exists = false;
                for(CategoryItem item : categories) {
                    if(item.getName().equals(newName)) { exists = true; break; }
                }

                if (!exists) {
                    // 기본 색상은 밝은 회색(Color.LIGHT_GRAY) 등으로 설정
                    CategoryItem newItem = new CategoryItem(newName, new Color(230, 230, 230));
                    categories.add(newItem);
                    listModel.addElement(newItem);
                    newCategoryField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "이미 존재하는 카테고리입니다.");
                }
            }
        } else if (source == colorButton) {
            // [NEW] 색상 변경 로직
            CategoryItem selected = categoryList.getSelectedValue();
            if (selected != null) {
                // 자바 스윙의 기본 컬러 선택기(JColorChooser) 띄우기
                Color newColor = JColorChooser.showDialog(this, "카테고리 색상 선택", selected.getColor());
                
                if (newColor != null) {
                    selected.setColor(newColor); // 데이터 업데이트
                    categoryList.repaint(); // 리스트 화면 즉시 갱신 (색상 반영)
                }
            } else {
                JOptionPane.showMessageDialog(this, "색상을 변경할 카테고리를 선택해주세요.");
            }

        } else if (source == deleteButton) {
            // [삭제]
            CategoryItem selected = categoryList.getSelectedValue();
            if (selected != null) {
                categories.remove(selected);
                listModel.removeElement(selected);
            }
        } else if (source == closeButton) {
            dispose();
        }
    }
}