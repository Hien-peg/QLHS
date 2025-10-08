/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.components;

/**
 *
 * @author minho
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.sgu.qlhs.ui.MainDashboard.*;

public class SidebarButton extends JComponent {
    private String text; private boolean active; private final List<ActionListener> listeners = new ArrayList<>();
    public SidebarButton(String text, boolean active){
        this.text=text; this.active=active; setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(9999, 44));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter(){ @Override public void mouseClicked(MouseEvent e){ fire(); }});
    }
    public void addActionListener(ActionListener l){ listeners.add(l); }
    private void fire(){ for(ActionListener l:listeners) l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text)); }
    public void setActive(boolean a){ this.active=a; repaint(); }
    @Override protected void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight();
        g2.setColor(active? SIDEBAR_BTN_ACTIVE : SIDEBAR_BTN);
        g2.fillRoundRect(6, 5, w-12, h-10, 12, 12);
        if(active){ g2.setColor(INDICATOR); g2.fillRoundRect(2, 8, 4, h-16, 8, 8); }
        g2.setColor(TEXT_WHITE); g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
        FontMetrics fm=g2.getFontMetrics(); int ty=(h+fm.getAscent()-fm.getDescent())/2;
        g2.drawString(text, 22, ty);
        g2.dispose();
    }
}
