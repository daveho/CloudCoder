// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.client.page;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.client.view.UserAdminUsersListView;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jspacco
 * 
 * TODO
 *    Button for bulk registration with a "browse for files" widget
 *
 */
public class UserAdminPage extends CloudCoderPage
{
    private static final long serialVersionUID = 1L;
    //private static final Logger logger=LoggerFactory.getLogger(UserAdminPage.class);
    
    private enum ButtonPanelAction {
        NEW("Add new user"),
        EDIT("Edit user"),
        DELETE("Delete user"),
        REGISTER_USERS("Register users");
        
        private String name;
        
        private ButtonPanelAction(String name) {
            this.name = name;
        }
        
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        public boolean isEnabledByDefault() {
            return this == NEW || this == REGISTER_USERS;
        }
    }
    private class UI extends Composite implements SessionObserver, Subscriber {
        private static final double USERS_BUTTON_BAR_HEIGHT_PX = 28.0;

        private PageNavPanel pageNavPanel;
        private Label courseLabel;
        private Button[] userManagementButtons;
        private UserAdminUsersListView userAdminUsersListView;
        private StatusMessageView statusMessageView;
        private AddUserPopupPanel popupPanel;
        
        public UI() {
            DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
            
            // Create a north panel with course info and a PageNavPanel
            LayoutPanel northPanel = new LayoutPanel();
            this.courseLabel = new Label();
            northPanel.add(courseLabel);
            northPanel.setWidgetLeftRight(courseLabel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
            northPanel.setWidgetTopHeight(courseLabel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
            courseLabel.setStyleName("cc-courseLabel");
            
            this.pageNavPanel = new PageNavPanel();
            northPanel.add(pageNavPanel);
            northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH, PageNavPanel.WIDTH_UNIT);
            northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT, PageNavPanel.HEIGHT_UNIT);
            
            dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT);
            
            // Create a center panel with user button panel and list of users 
            // registered for the given course.
            // Can eventually put other stuff here too.
            LayoutPanel centerPanel = new LayoutPanel();
            
            // Create a button panel with buttons for problem-related actions
            // (new problem, edit problem, make visible, make invisible, quiz, share)
            FlowPanel userButtonPanel = new FlowPanel();
            ButtonPanelAction[] actions = ButtonPanelAction.values();
            userManagementButtons = new Button[actions.length];
            for (final ButtonPanelAction action : actions) {
                final Button button = new Button(action.getName());
                userManagementButtons[action.ordinal()] = button;
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        switch (action) {
                        case NEW:
                            handleNewUser(event);
                            break;

                        case EDIT:
                            handleEditUser();
                            break;
                            
                        case REGISTER_USERS:
                            handleRegisterNewUsers();
                            break;
                            
                        case DELETE:
                            handleDeleteUser();
                            break;
                        }                    }
                });
                button.setEnabled(action.isEnabledByDefault());
                userButtonPanel.add(button);
            }
            
            centerPanel.add(userButtonPanel);
            centerPanel.setWidgetTopHeight(userButtonPanel, 0.0, Unit.PX, 28.0, Unit.PX);
            centerPanel.setWidgetLeftRight(userButtonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
            
            // Create users list
            this.userAdminUsersListView = new UserAdminUsersListView();
            centerPanel.add(userAdminUsersListView);
            centerPanel.setWidgetTopBottom(userAdminUsersListView, USERS_BUTTON_BAR_HEIGHT_PX, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            centerPanel.setWidgetLeftRight(userAdminUsersListView, 0.0, Unit.PX, 0.0, Unit.PX);
            
            // Create a StatusMessageView
            this.statusMessageView = new StatusMessageView();
            centerPanel.add(statusMessageView);
            centerPanel.setWidgetBottomHeight(statusMessageView, 0.0, Unit.PX, StatusMessageView.HEIGHT_PX, Unit.PX);
            centerPanel.setWidgetLeftRight(statusMessageView, 0.0, Unit.PX, 0.0, Unit.PX);
            
            dockLayoutPanel.add(centerPanel);
            
            initWidget(dockLayoutPanel);
        }
        
        private class AddUserPopupPanel extends PopupPanel{

            Button connect = new Button("Connect");
            Button delete = new Button("Delete");
            Button detail = new Button("Detail");

            public AddUserPopupPanel(final Widget widget){
               super(true);
               VerticalPanel vp = new VerticalPanel();
               //vp.add(connect);
               //vp.add(delete);
               //vp.add(detail);

               setWidget(vp);
               
               // TODO: Going to skip actually performing the submit
               // will instead call an async piece of code
               final FormPanel form = new FormPanel();
               form.setEncoding(FormPanel.ENCODING_MULTIPART);
               form.setMethod(FormPanel.METHOD_POST);
              
               // TODO are these style file hooks?
               form.addStyleName("table-center");
               form.addStyleName("demo-FormPanel");

               VerticalPanel holder = new VerticalPanel();

               // username
               holder.add(new Label("Username"));
               final TextBox username = new TextBox();
               username.setName("username");
               holder.add(username);
               
               // firstname
               holder.add(new Label("Firstname"));
               final TextBox firstname = new TextBox();
               firstname.setName("firstname");
               holder.add(firstname);
               
               // lastname
               holder.add(new Label("Lastname"));
               final TextBox lastname = new TextBox();
               lastname.setName("lastname");
               holder.add(lastname);
               
               // email
               holder.add(new Label("Email"));
               final TextBox email = new TextBox();
               email.setName("email");
               holder.add(email);

               // password
               holder.add(new Label("Password"));
               final PasswordTextBox passwd = new PasswordTextBox();
               passwd.setName("passwd");
               holder.add(passwd);
               
               // re-enter password
               holder.add(new Label("re-enter Password"));
               final PasswordTextBox passwd2 = new PasswordTextBox();
               passwd2.setName("passwd2");
               holder.add(passwd2);
               
               // TODO: Login mechanism?
               // should that be stored per user or per server?

               holder.add(new Button("Submit", new ClickHandler() {
                   @Override
                   public void onClick(ClickEvent event) {
                       //logger.info("Clicked submit button, trigger ClickHandler");
                       GWT.log("Clicked submit button, trigger ClickHandler, username is: "+username.getText());
                       // TODO don't submit; use asynchronous handler
                       // added into userService
                       //form.submit();
                       // then close the window
                   }
               }));
               form.add(holder);

               // form.setAction("url");

               form.addSubmitHandler(new FormPanel.SubmitHandler() {
                   @Override
                   public void onSubmit(SubmitEvent event) {
                       //logger.info("triggered SubmitHandler");
                       GWT.log("triggered submitHandler, username is "+username.getText());
                       
                   }
               });

               vp.add(form);
            }
        }
  
        
        /* (non-Javadoc)
         * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
         */
        public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
            
            // Activate views
            pageNavPanel.setBackHandler(new BackHomeHandler(session));
            pageNavPanel.setLogoutHandler(new LogoutHandler(session));
            userAdminUsersListView.activate(session, subscriptionRegistrar);
            statusMessageView.activate(session, subscriptionRegistrar);
            
            // The session should contain a course
            Course course = session.get(Course.class);
            courseLabel.setText(course.getName() + " - " + course.getTitle());
            session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        }
        
        @Override
        public void eventOccurred(Object key, Publisher publisher, Object hint) {
            if (key == Session.Event.ADDED_OBJECT && (hint instanceof User)) {
                onSelectUser((User) hint);
            }
            
        }
        
        private void onSelectUser(User user) {
            // Problem selected: enable/disable buttons appropriately
            userManagementButtons[ButtonPanelAction.EDIT.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.NEW.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.DELETE.ordinal()].setEnabled(true);
            userManagementButtons[ButtonPanelAction.REGISTER_USERS.ordinal()].setEnabled(true);
        }
        
        private void handleEditUser() {
            GWT.log("handle new user");
            final User chosen = getSession().get(User.class);
            Window.alert("Not implemented yet, sorry.");
            /*
            loadProblemAndTestCaseList(chosen, new ICallback<ProblemAndTestCaseList>() {
                @Override
                public void call(ProblemAndTestCaseList value) {
                    getSession().add(value);
                    getSession().notifySubscribers(Session.Event.EDIT_PROBLEM, value);
                }
            });
            */
        }
        
        private void handleDeleteUser() {
            GWT.log("Delete User not implemented");
            Window.alert("Not implemented yet, sorry");
        }
        
        private void handleNewUser(ClickEvent event) {
            GWT.log("handle new user");
            Window.alert("Not implemented yet, sorry.  You should use the command-line configuration features");
            /*
            Widget w = (Widget)event.getSource();
            AddUserPopupPanel pop = new AddUserPopupPanel(w);
            pop.setGlassEnabled(true);
            pop.center();
            pop.setPopupPosition(w.getAbsoluteLeft() - 150, w.getAbsoluteTop());
            pop.show();
            */
        }
        
        private void handleRegisterNewUsers() {
            Window.alert("Not implemented yet, sorry.  You should use the command-line configuration features");
            GWT.log("handle Register new users");
        }
    }
    
    private UI ui;

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
     */
    @Override
    public void createWidget() {
        ui = new UI();
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
     */
    @Override
    public void activate() {
        ui.activate(getSession(), getSubscriptionRegistrar());
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#deactivate()
     */
    @Override
    public void deactivate() {
        getSubscriptionRegistrar().cancelAllSubscriptions();
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#getWidget()
     */
    @Override
    public IsWidget getWidget() {
        return ui;
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.CloudCoderPage#isActivity()
     */
    @Override
    public boolean isActivity() {
        return true;
    }
}
