package com.mycompany.tokoberkahjayaa.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AutoSuggestor {

    private final JTextField textField;
    private final JPopupMenu popupMenu;
    private final JList<String> listSuggestions;
    private final DefaultListModel<String> listModel;
    private List<String> dictionary;
    private boolean isAdjusting = false;

    public AutoSuggestor(JTextField textField, Window container, List<String> dictionary) {
        this.textField = textField;
        this.dictionary = dictionary != null ? dictionary : new ArrayList<>();
        this.popupMenu = new JPopupMenu();
        this.listModel = new DefaultListModel<>();
        this.listSuggestions = new JList<>(listModel);

        init();
    }

    private void init() {
        popupMenu.setFocusable(false);
        listSuggestions.setFocusable(false);
        popupMenu.add(new JScrollPane(listSuggestions));

        listSuggestions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectSuggestion();
                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { 
                updateSuggestions(); 
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { 
                updateSuggestions(); 
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                updateSuggestions(); 
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (popupMenu.isShowing()) {
                    int index = listSuggestions.getSelectedIndex();
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        index++;
                        if (index >= listModel.getSize()) index = 0;
                        listSuggestions.setSelectedIndex(index);
                        listSuggestions.ensureIndexIsVisible(index);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        index--;
                        if (index < 0) index = listModel.getSize() - 1;
                        listSuggestions.setSelectedIndex(index);
                        listSuggestions.ensureIndexIsVisible(index);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        selectSuggestion();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        popupMenu.setVisible(false);
                    }
                }
            }
        });
    }

    public void updateDictionary(List<String> newDictionary) {
        this.dictionary = newDictionary;
    }

    private void updateSuggestions() {
        if (isAdjusting) return;

        SwingUtilities.invokeLater(() -> {
            String text = textField.getText();
            listModel.clear();

            if (text.trim().isEmpty()) {
                popupMenu.setVisible(false);
                return;
            }

            for (String item : dictionary) {
                if (item.toLowerCase().contains(text.toLowerCase())) {
                    listModel.addElement(item);
                }
            }

            if (listModel.getSize() > 0) {
                listSuggestions.setSelectedIndex(0);
                popupMenu.setPopupSize(textField.getWidth(), Math.min(120, listModel.getSize() * 24 + 10));
                if (!popupMenu.isShowing()) {
                    popupMenu.show(textField, 0, textField.getHeight());
                }
                textField.requestFocusInWindow();
            } else {
                popupMenu.setVisible(false);
            }
        });
    }

    private void selectSuggestion() {
        String selected = listSuggestions.getSelectedValue();
        if (selected != null) {
            isAdjusting = true;
            textField.setText(selected);
            popupMenu.setVisible(false);
            isAdjusting = false;
            
            textField.postActionEvent(); 
        }
    }
}