package com.plugins.mybaitslog.gui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColorChooser;
import com.plugins.mybaitslog.Config;
import com.plugins.mybaitslog.gui.compone.MyColorButton;
import com.plugins.mybaitslog.gui.compone.MyTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Map;

/**
 * 过滤设置 窗口
 *
 * @author lk
 * @version 1.0
 * @date 2020/8/23 17:14
 */
public class FilterSetting extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField preparingTextField;
    private JCheckBox startupCheckBox;
    private JPanel jpanel_select;
    private JPanel jpanel_update;
    private JPanel jpanel_delect;
    private JPanel jpanel_insert;
    private JPanel jpanel_other;
    private JTextArea addOpensTextArea;
    private JTable excludeTable;


    /**
     * 窗口初始化
     *
     * @param project 项目
     */
    public FilterSetting(Project project) {
        //设置标题
        this.setTitle("Filter Setting");
        this.setBackground(Color.WHITE);
        this.preparingTextField.setText(Config.Idea.getParameters());
        int startup = PropertiesComponent.getInstance(project).getInt(Config.Idea.DB_STARTUP_KEY, 1);
        startupCheckBox.setSelected(startup == 1);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        //region 自定义控件
        final MyTableModel myTableModel = new MyTableModel();
        final Map<String, Boolean> perRunMap = Config.Idea.getPerRunMap();
        for (Map.Entry<String, Boolean> next : perRunMap.entrySet()) {
            myTableModel.addRow(new Object[]{next.getKey(), next.getValue()});
        }
        this.excludeTable.setModel(myTableModel);

        addOpensTextArea.setText(String.join("\n", Config.Idea.getAddOpens()));
        String[] colorname = {"select", "update", "delete", "insert", "other"};
        for (String s : colorname) {
            final MyColorButton myColorButton = new MyColorButton(Config.Idea.getColor(s));
            myColorButton.addActionListener(e -> onColor(project, s, myColorButton));
            switch (s) {
                case "select":
                    this.jpanel_select.add(myColorButton);
                    break;
                case "update":
                    this.jpanel_update.add(myColorButton);
                    break;
                case "delete":
                    this.jpanel_delect.add(myColorButton);
                    break;
                case "insert":
                    this.jpanel_insert.add(myColorButton);
                    break;
                case "other":
                    this.jpanel_other.add(myColorButton);
                    break;
                default:
                    break;
            }
        }
        //endregion

        buttonOK.addActionListener(e -> onOK(project));
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void onColor(Project project, String type, MyColorButton myColorButton) {
        Color newColor = ColorChooser.chooseColor(this, "Choose Color", Color.white, true);
        if (null != newColor) {
            myColorButton.setColor(newColor);
            Config.Idea.setColor(type, newColor);
        }
    }

    /**
     * 点击确认按钮处理
     *
     * @param project 项目
     */
    private void onOK(Project project) {
        String preparing = this.preparingTextField.getText();
        String addOpens = this.addOpensTextArea.getText();
        final TableModel model = this.excludeTable.getModel();
        final int rowCount = model.getRowCount();
        for (int r = 0; r < rowCount; r++) {
            final Object key = model.getValueAt(r, 0);
            final Object value = model.getValueAt(r, 1);
            Config.Idea.setPerRunMap((String) key, (Boolean) value, true);
        }
        Config.Idea.setAddOpens(addOpens);
        Config.Idea.setParameters(preparing, Config.Idea.PARAMETERS);
        Config.Idea.setStartup(startupCheckBox.isSelected() ? 1 : 0);
        this.setVisible(false);
    }

    private void onCancel() {
        this.setVisible(false);
    }
}
