package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import Project.Common.TextFX;
import Project.Common.TextFX.Color;

public class UserListPanel extends JPanel {
    private JPanel userListArea;
    private static Logger logger = Logger.getLogger(UserListPanel.class.getName());

    public UserListPanel() {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it

        userListArea = content;

        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

        });
    }

    protected void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);
        UserListItem uli = new UserListItem(clientId, clientName);
        uli.setPreferredSize(new Dimension(userListArea.getWidth(), 20));
        uli.setMaximumSize(uli.getPreferredSize());
        // add to container
        userListArea.add(uli);
    }

    protected void removeUserListItem(long clientId) {
        logger.log(Level.INFO, "removing user list item for id " + clientId);
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                userListArea.remove(c);
                break;
            }
        }
    }

    protected void clearUserList() {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            userListArea.remove(c);
        }
    }

    protected void updateClientPoints(long clientId, String score) {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            System.out.println(TextFX.colorize("Checking panel", Color.BLUE));
            if (c instanceof UserListItem && c.getName().equals(clientId + "")) {
                System.out.println(TextFX.colorize("Found panel for points", Color.YELLOW));
                UserListItem uli = (UserListItem) c;
                uli.setPoints(Float.parseFloat(score));
                break;
            }
        }
    }

    //as4555 4/24/24
    protected void updateUserAwayStatus(long clientId, boolean away) {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                UserListItem uli = (UserListItem) c;
                uli.setAwayStatus(away);
                break;
            }
        }
    }

    //as4555 4/15/24
    protected void sortUserList() {
        Component[] cs = userListArea.getComponents();
        Arrays.sort(cs, Comparator.comparingDouble(c -> ((UserListItem) c).getPoints()).reversed());
        clearUserList();
        for (Component c : cs) {
            UserListItem uli = (UserListItem) c;
            addUserListItem(Long.parseLong(uli.getName()), uli.getUsernameContainer().getText());
            updateClientPoints(Long.parseLong(uli.getName()), String.valueOf(uli.getPoints()));
            //as4555 4/26/24
            updateUserAwayStatus(Long.parseLong(uli.getName()), uli.isAway());
            System.out.println(TextFX.colorize(uli.getUsernameContainer().getText() + ": " + uli.getPoints(), Color.RED));
        }
    }
}