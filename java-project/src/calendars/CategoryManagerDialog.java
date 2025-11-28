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


public class CategoryManagerDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JList<CategoryItem> categoryList;
    private DefaultListModel<CategoryItem> listModel;
    private JTextField newCategoryField;
    private JButton addButton, deleteButton, closeButton, colorButton;

    // --- 데이터 & DAO ---
    private Vector<CategoryItem> categories; // 원본 데이터 참조
    private CalendarDAO dao; // [NEW] DB 작업을 위한 DAO

    /**
     * 생성자: CalendarDAO를 추가로 받습니다.
     */
    public CategoryManagerDialog(Dialog parent, String title, Vector<CategoryItem> categories, CalendarDAO dao) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.categories = categories;
        this.dao = dao; // [NEW] DAO 저장

        // --- 1. 메인 패널 ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 2. 목록 패널 ---
        listModel = new DefaultListModel<>();
        listModel.addAll(categories);
        
        categoryList = new JList<>(listModel);
        categoryList.setCellRenderer(new CategoryRenderer()); // 렌더러 적용
        
        JScrollPane scrollPane = new JScrollPane(categoryList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. 입력 패널 ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        newCategoryField = new JTextField();
        addButton = new JButton("추가");
        
        inputPanel.add(new JLabel("새 카테고리:"), BorderLayout.WEST);
        inputPanel.add(newCategoryField, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // --- 4. 버튼 패널 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        colorButton = new JButton("색상 변경");
        deleteButton = new JButton("선택 삭제");
        closeButton = new JButton("닫기");

        buttonPanel.add(colorButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 이벤트 연결
        addButton.addActionListener(this);
        colorButton.addActionListener(this);
        deleteButton.addActionListener(this);
        closeButton.addActionListener(this);
        newCategoryField.addActionListener(this);

        setContentPane(mainPanel);
        setSize(400, 450);
        setLocationRelativeTo(null); // 화면 중앙 배치
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton || source == newCategoryField) {
            // [추가] DB에 저장 후 화면 갱신
            String newName = newCategoryField.getText().trim();
            if (!newName.isEmpty()) {
                // 중복 체크
                boolean exists = false;
                for(CategoryItem item : categories) {
                    if(item.getName().equals(newName)) { exists = true; break; }
                }

                if (!exists) {
                    Color defaultColor = new Color(230, 230, 230); // 기본 회색
                    
                    // [DB] 데이터베이스에 추가
                    if (dao.addCategory(newName, defaultColor)) {
                        // DB 저장 성공 시 UI 및 메모리 업데이트
                        CategoryItem newItem = new CategoryItem(newName, defaultColor);
                        categories.add(newItem);
                        listModel.addElement(newItem);
                        newCategoryField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "DB 저장 실패");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "이미 존재하는 카테고리입니다.");
                }
            }
            
        } else if (source == colorButton) {
            // [색상 변경] DB 업데이트 후 화면 갱신
            CategoryItem selected = categoryList.getSelectedValue();
            if (selected != null) {
                Color newColor = JColorChooser.showDialog(this, "카테고리 색상 선택", selected.getColor());
                
                if (newColor != null) {
                    // [DB] 색상 업데이트
                    if (dao.updateCategoryColor(selected.getName(), newColor)) {
                        // 성공 시 메모리 업데이트 및 다시 그리기
                        selected.setColor(newColor);
                        categoryList.repaint(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "색상 변경 실패");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "색상을 변경할 카테고리를 선택해주세요.");
            }

        } else if (source == deleteButton) {
            // [삭제] DB 삭제 후 화면 갱신
            CategoryItem selected = categoryList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "정말 삭제하시겠습니까? \n(해당 카테고리의 일정도 모두 삭제될 수 있습니다.)", 
                        "삭제 확인", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // [DB] 삭제
                    if (dao.deleteCategory(selected.getName())) {
                        // 성공 시 UI 제거
                        categories.remove(selected);
                        listModel.removeElement(selected);
                    } else {
                        JOptionPane.showMessageDialog(this, "삭제 실패");
                    }
                }
            }
        } else if (source == closeButton) {
            dispose();
        }
    }
}