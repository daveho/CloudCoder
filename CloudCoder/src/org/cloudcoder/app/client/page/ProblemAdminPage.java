// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import org.cloudcoder.app.client.model.PageId;
import org.cloudcoder.app.client.model.PageStack;
import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.client.view.ButtonPanel;
import org.cloudcoder.app.client.view.ChoiceDialogBox;
import org.cloudcoder.app.client.view.CourseAdminProblemListView;
import org.cloudcoder.app.client.view.IButtonPanelAction;
import org.cloudcoder.app.client.view.ImportCourseDialogBox;
import org.cloudcoder.app.client.view.ImportProblemDialog;
import org.cloudcoder.app.client.view.OkDialogBox;
import org.cloudcoder.app.client.view.PageNavPanel;
import org.cloudcoder.app.client.view.SetDatesDialogBox;
import org.cloudcoder.app.client.view.ShareManyProblemsDialog;
import org.cloudcoder.app.client.view.ShareProblemDialog;
import org.cloudcoder.app.client.view.StatusMessageView;
import org.cloudcoder.app.shared.dto.ShareExerciseStatus;
import org.cloudcoder.app.shared.dto.ShareExercisesResult;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseAndCourseRegistration;
import org.cloudcoder.app.shared.model.CourseSelection;
import org.cloudcoder.app.shared.model.ICallback;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndModule;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.ProblemLicense;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * Page for performing course admin actions related to {@link Problem}s.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class ProblemAdminPage extends CloudCoderPage {
	private static final int IMPORT_PROBLEMS_POLL_INTERVAL_MS = 2000;
	
	private enum ProblemAction implements IButtonPanelAction {
		NEW("New", "Create a new exercise"),
		EDIT("Edit", "Edit the selected exercise"),
		DELETE("Delete", "Delete the selected exercise"),
		STATISTICS("Statistics", "See statistics on selected exercise"),
		IMPORT("Import", "Import an exercise from the CloudCoder exercise repository"),
		SHARE("Share", "Shared selected exercise(s) by publishing them to the CloudCoder exercise repository"),
		IMPORT_COURSE("Import course", "Import all exercises from another course"),
		MAKE_VISIBLE("Make visible", "Make selected exerise(s) visible to students"),
		MAKE_INVISIBLE("Make invisible", "Make selected exercise(s) invisible to students"),
		SET_DATES("Set dates/times", "Configure when selected exercise(s) are assigned and due"),
		MAKE_PERMISSIVE("Make permissive", "Change license of exercises to a permissive Create Commons license"),
		QUIZ("Quiz", "Give selected exercise as a quiz");
		
		private final String name;
		private final String tooltip;
		
		private ProblemAction(String name, String tooltip) {
			this.name = name;
			this.tooltip = tooltip;
		}
		
		public String getName() {
			return name;
		}
		
		@Override
		public String getTooltip() {
			return tooltip;
		}
		
		public boolean isEnabledByDefault() {
			return this == NEW || this == IMPORT || this == IMPORT_COURSE;
		}
	}
	
	private enum DeleteChoice {
		CANCEL,
		DELETE,
	}
	
	private class UI extends Composite implements SessionObserver, Subscriber {
		private PageNavPanel pageNavPanel;
		private Label courseLabel;
		private ButtonPanel<ProblemAction> buttonPanel;
		private CourseAdminProblemListView courseAdminProblemListView;
		private StatusMessageView statusMessageView;

		public UI() {
			DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
			
			// Create a north panel with course info, PageNavPanel, and button panel
			LayoutPanel northPanel = new LayoutPanel();
			
			this.courseLabel = new Label();
			northPanel.add(courseLabel);
			northPanel.setWidgetLeftRight(courseLabel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(courseLabel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);
			courseLabel.setStyleName("cc-courseLabel");
			
			this.pageNavPanel = new PageNavPanel();
			northPanel.add(pageNavPanel);
			northPanel.setWidgetRightWidth(pageNavPanel, 0.0, Unit.PX, PageNavPanel.WIDTH_PX, Style.Unit.PX);
			northPanel.setWidgetTopHeight(pageNavPanel, 0.0, Unit.PX, PageNavPanel.HEIGHT_PX, Style.Unit.PX);

			// Create a button panel with buttons for problem-related actions
			buttonPanel = new ButtonPanel<ProblemAction>(ProblemAction.values()) {
				@Override
				public void onButtonClick(ProblemAction action) {
					onProblemButtonClick(action);
				}
				
				/* (non-Javadoc)
				 * @see org.cloudcoder.app.client.view.ButtonPanel#isEnabled(org.cloudcoder.app.client.view.IButtonPanelAction)
				 */
				@Override
				public boolean isEnabled(ProblemAction action) {
				    Problem[] problems=getSession().get(Problem[].class);
				    if (problems==null) {
				        return false;
				    }
				    int numSelected=problems.length;
				    /*
				     * New and Import don't affect selected problems so we don't
				     * care how many problems are selected.
				     * 
				     * Sharing, Changing Visibility, and Making Permissive can work 
				     * on multiple exercises
				     * 
				     * Delete, Edit, Quiz, and Statistics only work on one exercise at 
				     * a time.
				     */
				    switch (action) {
				    case NEW:
				    case IMPORT:
				    case MAKE_VISIBLE:
				    case MAKE_INVISIBLE:
                    case SHARE:
                    case MAKE_PERMISSIVE:
				        return true;
                    case DELETE:
                    case EDIT:
                    case QUIZ:
                    case STATISTICS:
                        return (numSelected==1);
                    default:
                        return true;
				    }
				}
			};
			
			northPanel.add(buttonPanel);
			northPanel.setWidgetTopHeight(buttonPanel, PageNavPanel.HEIGHT_PX, Unit.PX, ButtonPanel.HEIGHT_PX, Unit.PX);
			northPanel.setWidgetLeftRight(buttonPanel, 0.0, Unit.PX, 0.0, Unit.PX);
			
			dockLayoutPanel.addNorth(northPanel, PageNavPanel.HEIGHT_PX + ButtonPanel.HEIGHT_PX + 10.0);
			
			// Create a south panel with a StatusMessageView
			this.statusMessageView = new StatusMessageView();
			dockLayoutPanel.addSouth(statusMessageView, StatusMessageView.HEIGHT_PX);
			
			// Create a center panel with problems list.
			this.courseAdminProblemListView = new CourseAdminProblemListView(ProblemAdminPage.this);
			dockLayoutPanel.add(courseAdminProblemListView);
			// Handle edits to the module name.
			courseAdminProblemListView.setEditModuleNameCallback(new ICallback<ProblemAndModule>() {
				public void call(ProblemAndModule value) {
					setProblemModuleName(value);
				}
			});
			
			initWidget(dockLayoutPanel);
		}

		private void setProblemModuleName(final ProblemAndModule value) {
			RPC.getCoursesAndProblemsService.setModule(value.getProblem(), value.getModule().getName(), new AsyncCallback<Module>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							@Override
							public void run() {
								setProblemModuleName(value);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not set module for exercise", caught));
					}
				}

				@Override
				public void onSuccess(Module result) {
					getSession().add(StatusMessage.goodNews("Successfully changed module for exercise"));
				}
			});
		}

		/**
		 * Called when a problem button is clicked.
		 * 
		 * @param action the ProblemButtonAction
		 */
		protected void onProblemButtonClick(ProblemAction action) {
			switch (action) {
			case NEW:
				handleNewProblem();
				break;

			case EDIT:
				handleEditProblem();
				break;
				
			case DELETE:
				handleDeleteProblem();
				break;

			case STATISTICS:
				handleStatistics();
				break;
				
			case SHARE:
				doShareProblem2();
				break;
				
			case IMPORT:
				doImportProblem();
				break;
				
			case IMPORT_COURSE:
				doImportCourse();
				break;
				
			case MAKE_VISIBLE:
			case MAKE_INVISIBLE:
				doChangeVisibility(action == ProblemAction.MAKE_VISIBLE);
				break;
				
			case SET_DATES:
				doSetDates();
				break;
				
			case MAKE_PERMISSIVE:
			    doMakePermissive();
			    break;
				
			case QUIZ:
				handleQuiz();
				break;
			}
		}

		private void doChangeVisibility(final boolean visible) {
			Problem[] chosen = getSession().get(Problem[].class);
			final Course course = getCurrentCourse();
			
			getSession().add(StatusMessage.pending("Changing visibility of problem..."));
			
			// Would like to send problems in bulk and fetch test cases server-side
			for (Problem problem : chosen) {
			    SessionUtil.loadProblemAndTestCaseList(ProblemAdminPage.this, problem, new ICallback<ProblemAndTestCaseList>() {
	                /* (non-Javadoc)
	                 * @see org.cloudcoder.app.shared.model.ICallback#call(java.lang.Object)
	                 */
	                @Override
	                public void call(ProblemAndTestCaseList value) {
	                    value.getProblem().setVisible(visible);
	                    updateProblem(value, course);
	                }
	            },
	            new ICallback<Pair<String,Throwable>>() {
	            	@Override
	            	public void call(Pair<String, Throwable> value) {
	            		getSession().add(StatusMessage.error(value.getLeft(), value.getRight()));
	            	}
				});
            }
		}
		
		private void doMakePermissive() {
		    Problem[] chosen = getSession().get(Problem[].class);
            final Course course = getCurrentCourse();
            
            getSession().add(StatusMessage.pending("Changing visibility of problem..."));
            
            // Would like to send problems in bulk and fetch test cases server-side
            for (Problem problem : chosen) {
                SessionUtil.loadProblemAndTestCaseList(ProblemAdminPage.this, problem, new ICallback<ProblemAndTestCaseList>() {
                    /* (non-Javadoc)
                     * @see org.cloudcoder.app.shared.model.ICallback#call(java.lang.Object)
                     */
                    @Override
                    public void call(ProblemAndTestCaseList value) {
                        value.getProblem().setLicense(ProblemLicense.CC_ATTRIB_SHAREALIKE_3_0);
                        updateProblem(value, course);
                    }
                },
                new ICallback<Pair<String,Throwable>>() {
                	@Override
                	public void call(Pair<String, Throwable> value) {
                		getSession().add(StatusMessage.error(value.getLeft(), value.getRight()));
                	}
				});
            }
		}
		
		private void doSetDates() {
			final SetDatesDialogBox dialog = new SetDatesDialogBox();
			
			Runnable callback = new Runnable() {
				@Override
				public void run() {
					long whenAssigned = dialog.getWhenAssigned();
					long whenDue = dialog.getWhenDue();
					Problem[] selected = getSession().get(Problem[].class);
					//getSession().add(StatusMessage.information("Should be setting dates"));
					for (Problem problem : selected) {
						problem.setWhenAssigned(whenAssigned);
						problem.setWhenDue(whenDue);
					}
					doUpdateProblemDates(selected);
				}
			};
			
			dialog.setOnSetDatesCallback(callback);
			dialog.center();
		}

		private void doUpdateProblemDates(final Problem[] selected) {
			RPC.getCoursesAndProblemsService.updateProblemDates(selected, new AsyncCallback<OperationResult>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							@Override
							public void run() {
								doUpdateProblemDates(selected);
							}
						});
					} else {
						addSessionObject(StatusMessage.error("Could not update exercises", caught));
					}
				}
				
				@Override
				public void onSuccess(OperationResult result) {
					addSessionObject(StatusMessage.fromOperationResult(result));
					reloadProblems(getCurrentCourse());
				}
			});;
		}
		
		private void doShareProblem2() {
		    final Problem[] chosen=getSession().get(Problem[].class);
		    if (chosen.length == 0) {
		    	// No problems selected, so nothing to do 
		    	return;
		    }
		    
		    GWT.log("Selected "+chosen.length+" problems in the UI");
		    // Filter the problems
		    // We cannot upload anything that is not permissive, 
		    // is imported but unchanged,
		    // or has already been shared
		    for (Problem p : chosen) {
		        if (!p.getLicense().isPermissive()) {
		            GWT.log("License: "+p.getLicense().toString()+" is not permissive, wtf?");
		            OkDialogBox licenseDialog = new OkDialogBox(
	                        "Sharing requires a permissive license",
	                        "Sharing a problem requires a permissive license. Please ensure shared problems " +
	                        "have a permissive license such as Creative Commons or GNU FDL.");
	                licenseDialog.center();
	                return;
		        } else if (p.getProblemAuthorship() == ProblemAuthorship.IMPORTED) {
		            GWT.log("authorship is imported: "+p.getProblemAuthorship().toString());
		            OkDialogBox problemAuthorshipDialog = new OkDialogBox(
		                    "Sharing not allowed for unmodified problems",
		                    "At least one problem was imported from, or shared to, the exercise repository, " +
		                    "but has not been modified. There is no reason to share them until they are changed. " +
		                    "You can share it if you make some changes first.");
		            problemAuthorshipDialog.center();
		            return;
		        } else if (p.isShared()) {
		            // XXX I'm pretty sure this case should never happen
		            GWT.log("share status: "+p.isShared()+" and problem authorship: "+p.getProblemId());
		            OkDialogBox problemSharingDialog = new OkDialogBox(
                            "Sharing not allowed for problems that have already been shared",
                            "This problem was imported from the exercise repository but not modified, " +
                            "or exported to the repository and not modified. " +
		                    "You can share it if you make some changes first.");
                    problemSharingDialog.center();
                    return;
		        }
		    }
		    
		    if (chosen.length == 1) {
		    	// Single problem selected.  This is a special case at the moment because
		    	// the single-problem dialog gives more specific feedback about which
		    	// problem is being shared and what the license is.
		    	shareOne(chosen);
		    } else {
		    	// Multiple problems selected.
			    shareMany(chosen);
		    }
		}

		/**
		 * @param chosen
		 */
		private void shareOne(final Problem[] chosen) {
			SessionUtil.loadProblemAndTestCaseList(
					ProblemAdminPage.this,
					chosen[0],
					new ICallback<ProblemAndTestCaseList>() {
						@Override
						public void call(ProblemAndTestCaseList value) {
					    	ShareProblemDialog dialog = new ShareProblemDialog();
							dialog.setExercise(value);
							dialog.setResultCallback(new ICallback<OperationResult>() {
								@Override
								public void call(OperationResult value) {
									// Add a StatusMessage with the result of the operation
									GWT.log("share problem result: " + value.isSuccess() + ":" + value.getMessage());

									if (value.isSuccess()) {
										getSession().add(StatusMessage.goodNews(value.getMessage()));
										
										// Reload the problems so that the shared flag is updated
										// for the problem the user just shared
										reloadProblems(getCurrentCourse());
									} else {
										getSession().add(StatusMessage.error(value.getMessage()));
									}
								}
							});
							dialog.center();
						}
					},
					new ICallback<Pair<String, Throwable>>() {
						@Override
						public void call(Pair<String, Throwable> value) {
							getSession().add(StatusMessage.error(value.getLeft(), value.getRight()));
						}
					}
			);
		}

		/**
		 * @param chosen
		 */
		private void shareMany(final Problem[] chosen) {
			ShareManyProblemsDialog shareManyProblemsDialog=new ShareManyProblemsDialog();
		    shareManyProblemsDialog.setExercise(chosen);
		    shareManyProblemsDialog.setResultCallback(new ICallback<ShareExercisesResult>() {
		        public void call(ShareExercisesResult result) {
		            // Add a StatusMessage with the result of the operation
		            GWT.log("share problem result: " + result.getStatus() + ":" + result.getMessage());

		            if (result.getStatus()==ShareExerciseStatus.ALL_OK) {
		                getSession().add(StatusMessage.goodNews(result.getMessage()));
		            } else {
		                int numShared=result.getNumSharedSuccessfully();
		                if (numShared>0) {
		                    getSession().add(StatusMessage.error("Successfuly shared "+numShared+" results before error: "+result.getMessage()));
		                } else {
		                    getSession().add(StatusMessage.error(result.getMessage()));
		                }
		            }
		            // Reload the problems so that the shared flag is updated
                    // for the problem the user just shared
                    reloadProblems(getCurrentCourse());
		        }
		    });
		    shareManyProblemsDialog.center();
		}

		private void doImportProblem() {
			ImportProblemDialog dialog = new ImportProblemDialog();
			final Course course = getCurrentCourse();
			dialog.setCourse(course);
			dialog.setResultCallback(new ICallback<ProblemAndTestCaseList>() {
				@Override
				public void call(ProblemAndTestCaseList value) {
					if (value != null) {
						getSession().add(StatusMessage.goodNews("Exercise imported successfully!"));
						
						reloadProblems(course);
					} else {
						getSession().add(StatusMessage.error("Exercise was not found"));
					}
				}
			});
			
			dialog.center();
		}
		
		private void doImportCourse() {
			GWT.log("Import all problems from course");
			
			// Get all course registrations for user
			SessionUtil.loadCourseAndCourseRegistrationList(
					ProblemAdminPage.this,
					// success callback
					new ICallback<CourseAndCourseRegistration[]>() {
						@Override
						public void call(CourseAndCourseRegistration[] value) {
							getSession().add(value);
							
							// Create dialog
							final ImportCourseDialogBox dialog = new ImportCourseDialogBox(getSession());
							dialog.setSelectCourseCallback(new ICallback<CourseAndCourseRegistration>() {
								@Override
								public void call(CourseAndCourseRegistration value) {
									doImportProblemsFromCourse(value, dialog);
								}
							});
							dialog.center();
						}
					},
					// failure callback
					new ICallback<Pair<String, Throwable>>() {
						@Override
						public void call(Pair<String, Throwable> value) {
							getSession().add(StatusMessage.error(value.getLeft(), value.getRight()));
						}
					}
			);
		}
		
		private void doImportProblemsFromCourse(final CourseAndCourseRegistration source, final ImportCourseDialogBox dialog) {
			CourseSelection dest = getSession().get(CourseSelection.class);
			
			GWT.log("TODO: Import problems from " + source.getCourse().getNameAndTitle());
			
			RPC.getCoursesAndProblemsService.startImportAllProblemsFromCourse(source.getCourse(), dest.getCourse(), new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					addSessionObject(StatusMessage.pending("Importing exercises, please wait..."));
					doCheckImportProblemsFromCourse(dialog);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							@Override
							public void run() {
								doImportProblemsFromCourse(source, dialog);
							}
						});
					} else {
						addSessionObject(StatusMessage.error("Error importing exercises", caught));
						dialog.hide();
					}
				}
			});
		}

		private void doCheckImportProblemsFromCourse(final ImportCourseDialogBox dialog) {
			// Poll for completion of importing problems from course
			new Timer() {
				@Override
				public void run() {
					RPC.getCoursesAndProblemsService.checkImportAllProblemsFromCourse(new AsyncCallback<OperationResult>() {
						@Override
						public void onSuccess(OperationResult result) {
							if (result != null) {
								// Completed
								addSessionObject(StatusMessage.fromOperationResult(result));
								dialog.hide();
								
								// Reload problems
								courseAdminProblemListView.loadProblems(getSession(), getSession().get(CourseSelection.class).getCourse());
							} else {
								// No result yet, wait and try again
								doCheckImportProblemsFromCourse(dialog);
							}
						}
						
						@Override
						public void onFailure(Throwable caught) {
							addSessionObject(StatusMessage.error("Could not import exercises from course", caught));
							dialog.hide();
						}
					});
				}
			}.schedule(IMPORT_PROBLEMS_POLL_INTERVAL_MS);
		}

		private void handleEditProblem() {
			// Get the full ProblemAndTestCaseList for the chosen Problem
			final Problem chosen = getSession().get(Problem.class);
			
			if (chosen==null) {
			    return;
			}
			
			SessionUtil.loadProblemAndTestCaseList(ProblemAdminPage.this, chosen, new ICallback<ProblemAndTestCaseList>() {
				@Override
				public void call(ProblemAndTestCaseList value) {
					getSession().add(value);
					getSession().get(PageStack.class).push(PageId.EDIT_PROBLEM);
				}
			},
			new ICallback<Pair<String,Throwable>>() {
				public void call(Pair<String,Throwable> value) {
					getSession().add(StatusMessage.error(value.getLeft(), value.getRight()));
				}
			});
		}
		
		private void handleDeleteProblem() {
			final Problem chosen = getSession().get(Problem.class);
			final Course course = getCurrentCourse();
			
			// Only invisible problems may be deleted
			if (chosen.isVisible()) {
				OkDialogBox visibleDialog = new OkDialogBox(
						"Problem is visible",
						"You can't delete a problem which is visible to students. Make it invisible first.");
				visibleDialog.center();
				return;
			}
			
			// Confirm using a dialog
			ChoiceDialogBox<DeleteChoice> confirmDeleteDialog = new ChoiceDialogBox<DeleteChoice>(
					"Really delete problem?",
					"Do you really want to delete the selected problem (" + chosen.getTestname() + ")? " +
					"If you click 'Delete problem' there will be no way to undo the deletion.",
					new ChoiceDialogBox.ChoiceHandler<DeleteChoice>() {
						@Override
						public void handleChoice(DeleteChoice choice) {
							if (choice == DeleteChoice.DELETE) {
								getSession().add(StatusMessage.pending("Deleting problem..."));
								deleteProblem(chosen, course);
							}
						}
					}
			);
			confirmDeleteDialog.addChoice("Cancel", DeleteChoice.CANCEL);
			confirmDeleteDialog.addChoice("Delete problem", DeleteChoice.DELETE);
			confirmDeleteDialog.center();
		}

		/**
		 * @author Andrei Papancea
		 * 
		 * Updated: 9/17/2012 at 3:44 PM
		 * 
		 */
		private void handleStatistics() {
			// Switch to the StatisticsPage
			getSession().get(PageStack.class).push(PageId.STATISTICS);
		}
		
		private void handleNewProblem() {
			Problem problem = new Problem();
			Problem.initEmpty(problem);
			
			// Set default when assigned and when due dates/times
			// (assigned now, due in 48 hours)
			problem.setWhenAssigned(System.currentTimeMillis());
			problem.setWhenDue(problem.getWhenAssigned() + (48L*60L*60L*1000L));
			
			// Set author name, email, and website based on User information
			User user = getSession().get(User.class);
			problem.setAuthorName(user.getFirstname() + " " + user.getLastname());
			problem.setAuthorEmail(user.getEmail());
			problem.setAuthorWebsite(user.getWebsite());
			
			// Set course id
			problem.setCourseId(getCurrentCourse().getId());
			
			// Initially there are no test cases
			TestCase[] testCaseList= new TestCase[0];
			
			// Edit it!
			ProblemAndTestCaseList problemAndTestCaseList = new ProblemAndTestCaseList();
			problemAndTestCaseList.setProblem(problem);
			problemAndTestCaseList.setTestCaseList(testCaseList);
			getSession().add(problemAndTestCaseList);
			getSession().get(PageStack.class).push(PageId.EDIT_PROBLEM);
		}
		
		private void handleQuiz() {
			Problem selected = getSession().get(Problem.class);
			if (selected != null) {
				if (selected.isVisible()) {
					getSession().add(StatusMessage.error("Quiz problems must not be visible to students!"));
					return;
				}
				
				// Switch to the Quiz page
				getSession().get(PageStack.class).push(PageId.QUIZ);
			}
		}

		public void activate(Session session, SubscriptionRegistrar subscriptionRegistrar) {
			session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
			
			// Activate views
			pageNavPanel.setBackHandler(new PageBackHandler(session));
			pageNavPanel.setLogoutHandler(new LogoutHandler(session));
			courseAdminProblemListView.activate(session, subscriptionRegistrar);
			if (courseAdminProblemListView.hasPotentialUnsharedExercises())
			{
			    // polite nagging in the UI
			    getSession().add(StatusMessage.information("You have unshared exercises! "+
			            "Please consider sharing them to the cloudcoder exercise repository!"));
			}
			statusMessageView.activate(session, subscriptionRegistrar);
			
			// The session should contain a course
			Course course = getCurrentCourse();
			courseLabel.setText("Problems in " + course.getNameAndTitle());
		}

		/* (non-Javadoc)
		 * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
		 */
		@Override
		public void eventOccurred(Object key, Publisher publisher, Object hint) {
			if (key == Session.Event.ADDED_OBJECT && (hint instanceof Problem[])) {
				// Problem selected: enable/disable buttons appropriately
				buttonPanel.updateButtonEnablement();
			}
		}

		public void deleteProblem(final Problem chosen, final Course course) {
			RPC.getCoursesAndProblemsService.deleteProblem(course, chosen, new AsyncCallback<OperationResult>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								deleteProblem(chosen, course);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Error deleting problem: " + caught.getMessage()));
					}
				}
				@Override
				public void onSuccess(OperationResult result) {
					if (result.isSuccess()) {
						getSession().add(StatusMessage.goodNews(result.getMessage()));
						reloadProblems(course);
					} else {
						getSession().add(StatusMessage.error(result.getMessage()));
					}
				}
			});
		}

		public void updateProblem(final ProblemAndTestCaseList value, final Course course) {
			RPC.getCoursesAndProblemsService.storeProblemAndTestCaseList(value, course, new AsyncCallback<ProblemAndTestCaseList>() {
				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof CloudCoderAuthenticationException) {
						recoverFromServerSessionTimeout(new Runnable() {
							public void run() {
								// Try again!
								updateProblem(value, course);
							}
						});
					} else {
						getSession().add(StatusMessage.error("Could not update problem visibility: " + caught.getMessage()));
					}
				}
				
				/* (non-Javadoc)
				 * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
				 */
				@Override
				public void onSuccess(ProblemAndTestCaseList result) {
					getSession().add(StatusMessage.goodNews("Problem visibility updated successfully"));
					reloadProblems(course);
				}
			});
		}

		public void reloadProblems(final Course course) {
			// Reload problems
			SessionUtil.loadProblemAndSubmissionReceiptsInCourse(ProblemAdminPage.this, course, getSession());
			
			// If a problem is selected, add it to the session
			// (so the buttons are enabled/disable appropriately).
			Problem[] problems=courseAdminProblemListView.getSelected();
			if (problems != null) {
				getSession().add(problems);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#createWidget()
	 */
	@Override
	public void createWidget() {
		setWidget(new UI());
	}
	
	@Override
	public Class<?>[] getRequiredPageObjects() {
		return new Class<?>[]{ CourseSelection.class };
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.page.CloudCoderPage#activate()
	 */
	@Override
	public void activate() {
		((UI)getWidget()).activate(getSession(), getSubscriptionRegistrar());
	}
	
	@Override
	public PageId getPageId() {
		return PageId.PROBLEM_ADMIN;
	}

	@Override
	public void initDefaultPageStack(PageStack pageStack) {
		pageStack.push(PageId.COURSES_AND_PROBLEMS);
	}
}
