/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Ryan D. Brooks
 */
public enum FrameworkImage implements OseeImage {
   ACCEPT("accept.gif"),
   ADMIN("admin.gif"),
   ADD_GREEN("add.gif"),
   ARCHIVE("archive.gif"),
   ARTIFACT_EDITOR("artifact_editor.gif"),
   ARTIFACT_EXPLORER("artifact_explorer.gif"),
   ARTIFACT_IMPORT_WIZARD("artifact_import_wiz.png"),
   ARTIFACT_SEARCH("artifact_search.gif"),
   ARTIFACT_VERSION("artifact_version.gif"),
   ARROW_RIGHT_YELLOW("nav_forward.gif"),
   ARROW_UP_YELLOW("up.gif"),
   ARROW_DOWN_YELLOW("down.gif"),
   ATTRIBUTE_MOLECULE("molecule.gif"),
   ATTRIBUTE_SUB_A("attribute.gif"),
   ATTRIBUTE_DISABLED("disabled_attribute.gif"),
   APPLICATION_SERVER("appserver.gif"),
   AUTHENTICATED("authenticated.gif"),
   BACK("back.gif"),
   BLAM("blam.gif"),
   BRANCH_CHANGE("branch_change.gif"),
   BRANCH("branch.gif"),
   BRANCH_SYSTEM_ROOT("branchYellow.gif"),
   BRANCH_BASELINE("baseline.gif"),
   BRANCH_CHANGE_DEST("branch_change_dest.gif"),
   BRANCH_CHANGE_SOURCE("branch_change_source.gif"),
   BRANCH_IN_CREATION_OVERLAY("waiting_over.gif"),
   BRANCH_MERGE("merge.gif"),
   BRANCH_WORKING("working.gif"),
   BRANCH_FAVORITE_OVERLAY("star_9_9.gif"),
   BRANCH_CHANGE_MANAGED("change_managed_branch.gif"),
   BRANCH_COMMIT("commitBranch.gif"),
   BUG("bug.gif"),
   CHECKBOX_ENABLED("chkbox_enabled.gif"),
   CHECKBOX_DISABLED("chkbox_disabled.gif"),
   CLOCK("clock.gif"),
   COLLAPSE_ALL("collapseAll.gif"),
   CONFLICTING_Deleted("CONFLICTING_Deleted.gif"),
   CONFLICTING_Modified("CONFLICTING_Modified.gif"),
   CONFLICTING_New("CONFLICTING_New.gif"),
   COPYTOCLIPBOARD("copyToClipboard.gif"),
   CUSTOMIZE("customize.gif"),
   DB_ICON_BLUE("DBiconBlue.GIF"),
   DB_ICON_BLUE_EDIT("DBiconBlueEdit.gif"),
   DELETE("delete.gif"),
   DELETE_EDIT("delete_edit.gif"),
   DIRTY("dirty.gif"),
   DOT_RED("red_light.gif"),
   DOT_YELLOW("yellow_light.gif"),
   DOT_GREEN("green_light.gif"),
   DUPLICATE("duplicate.gif"),
   EDIT("edit.gif"),
   EDIT2("edit2.gif"),
   EDIT_BLUE("DBiconBlueEdit.GIF"),
   EDIT_ARTIFACT("edit_artifact.gif"),
   EMAIL("email.gif"),
   ERROR("errorRound.gif"),
   ERROR_OVERLAY("error.gif"),
   EXPAND_ALL("expandAll.gif"),
   EXPORT_DATA("export_data.gif"),
   EXPORT_TABLE("export_table.gif"),
   EXCLAIM_RED("redExclaim.gif"),
   FLASHLIGHT("flashlight.gif"),
   FILTERS("filter.gif"),
   FOLDER("folder.gif"),
   GEAR("gear.gif"),
   GREEN_PLUS("greenPlus.gif"),
   GROUP("group.gif"),
   HEADING("heading.gif"),
   HELP("help.gif"),
   IMPORT("import.gif"),
   INCOMING_ARTIFACT_DELETED("INCOMING_Deleted.gif"),
   INCOMING_DELETED("INCOMING_Deleted.gif"),
   INCOMING_INTRODUCED("INCOMING_New.gif"),
   INCOMING_MODIFIED("INCOMING_Modified.gif"),
   INCOMING_NEW("INCOMING_New.gif"),
   INFO_SM("info_sm.gif"),
   INFO_LG("info_lg.gif"),
   LASER("laser_16_16.gif"),
   LASER_OVERLAY("laser_8_8.gif"),
   LINE_MATCH("line_match.gif"),
   LOAD("load.gif"),
   LOCKED_KEY("lockkey.gif"),
   LOCKED_NO_ACCESS("red_lock.gif"),
   LOCKED_WITH_ACCESS("green_lock.gif"),
   NOT_EQUAL("not_equal.gif"),
   NAV_BACKWARD("nav_backward.gif"),
   NAV_FORWARD("nav_forward.gif"),
   MAGNIFY("magnify.gif"),
   MISSING("missing"),
   MERGE("merge.gif"),
   MERGE_SOURCE("green_s.gif"),
   MERGE_DEST("blue_d.gif"),
   MERGE_YELLOW_M("yellow_m.gif"),
   MERGE_START("conflict.gif"),
   MERGE_INFO("issue.gif"),
   MERGE_MARKED("chkbox_enabled.gif"),
   MERGE_EDITED("chkbox_disabled.gif"),
   MERGE_OUT_OF_DATE("chkbox_red.gif"),
   MERGE_OUT_OF_DATE_COMMITTED("chkbox_enabled_conflicted.gif"),
   MERGE_NO_CONFLICT("accept.gif"),
   MERGE_NOT_RESOLVEABLE("red_light.gif"),
   MERGE_SUCCESS("icon_success.gif"),
   MERGE_CAUTION("icon_warning.gif"),
   NARRITIVE("narrative.gif"),
   OPEN("open.gif"),
   OUTGOING_ARTIFACT_DELETED("OUTGOING_Deleted.gif"),
   OUTGOING_DELETED("OUTGOING_Deleted.gif"),
   OUTGOING_INTRODUCED("OUTGOING_New.gif"),
   OUTGOING_MERGED("branch_merge.gif"),
   OUTGOING_MODIFIED("OUTGOING_Modified.gif"),
   OUTGOING_NEW("OUTGOING_New.gif"),
   OUTLINE("outline_co.gif"),
   PROBLEM("greenBug.gif"),
   PREVIEW_ARTIFACT("preview_artifact.gif"),
   PRINT("print.gif"),
   PURPLE("purple.gif"),
   RECTANGLE_16("rectangle16.gif"),
   RECTANGLE_24("rectangle24.gif"),
   REFRESH("refresh.gif"),
   RELATION("relate.gif"),
   RELOAD("reload.gif"),
   REMOVE("remove.gif"),
   REMOVE_ALL("removeAll.gif"),
   REJECT("reject.gif"),
   ROOT_HIERARCHY("package_obj.gif"),
   RUN_EXC("run_exc.gif"),
   PAGE("page.gif"),
   RULE("rule.gif"),
   SAVE_NEEDED("save.gif"),
   SAVED("saved.gif"),
   SAVE("save.gif"),
   SEVERITY_MAJOR("major.gif"),
   SEVERITY_MINOR("minor.gif"),
   SEVERITY_ISSUE("issue.gif"),
   SLASH_RED_OVERLAY("red_slash.gif"),
   SWITCHED("switched.gif"),
   TRASH("trash.gif"),
   SKYWALKER("skywalker.gif"),
   SUPPORT("users2.gif"),
   URL("www.gif"),
   USER("userPurple.gif"),
   USER_PURPLE("userPurple.gif"),
   USER_RED("userRed.gif"),
   USER_GREY("userGrey.gif"),
   USER_YELLOW("userYellow.gif"),
   USER_ADD("userAdd.gif"),
   USERS("users2.gif"),
   UN_ARCHIVE("unarchive.gif"),
   VERSION("version.gif"),
   WORKFLOW("workflow.gif"),
   WIDGET("widget.gif"),
   WARNING("warn.gif"),
   WARNING_OVERLAY("alert_8_8.gif"),
   WWW("www.gif"),
   X_RED("redRemove.gif");

   private final String fileName;

   private FrameworkImage(String fileName) {
      this.fileName = fileName;
   }

   @Override
   public ImageDescriptor createImageDescriptor() {
      if (this == MISSING) {
         return ImageDescriptor.getMissingImageDescriptor();
      }
      return ImageManager.createImageDescriptor(SkynetGuiPlugin.PLUGIN_ID, "images", fileName);
   }

   @Override
   public String getImageKey() {
      return SkynetGuiPlugin.PLUGIN_ID + "." + fileName;
   }
}