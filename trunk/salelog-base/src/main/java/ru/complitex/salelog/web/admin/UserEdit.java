package ru.complitex.salelog.web.admin;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.admin.Module;
import org.complitex.admin.service.UserBean;
import org.complitex.admin.strategy.UserInfoStrategy;
import org.complitex.dictionary.entity.*;
import org.complitex.dictionary.service.LocaleBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.PreferenceBean;
import org.complitex.dictionary.util.CloneUtil;
import org.complitex.dictionary.web.component.DomainObjectInputPanel;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.template.web.component.LocalePicker;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.ADMINISTRATORS;
import static org.complitex.dictionary.entity.UserGroup.GROUP_NAME.EMPLOYEES;
import static org.complitex.dictionary.web.DictionaryFwSession.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.07.2010 14:12:33
 *
 *  Страница создания и редактирования пользователя
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class UserEdit extends FormTemplatePage {

    private static final Logger log = LoggerFactory.getLogger(UserEdit.class);
    @EJB
    private UserBean userBean;
    @EJB
    private UserInfoStrategy userInfoStrategy;
    @EJB
    private LogBean logBean;
    @EJB
    private PreferenceBean preferenceBean;
    @EJB
    private LocaleBean localeBean;

    public UserEdit() {
        super();
        init(null, false);
    }

    public UserEdit(PageParameters parameters) {
        super();
        init(parameters.get("user_id").toOptionalLong(), "copy".equals(parameters.get("action").toString()));
    }

    private void init(Long userId, boolean copyUser) {
        add(new Label("title", new ResourceModel("title")));
        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        //Модель данных
        User user = userId != null ? userBean.getUser(userId) : userBean.newUser();
        final IModel<User> userModel = new Model<User>(user);

        final SearchComponentState searchComponentState = new SearchComponentState();
        Locale userLocale = null;
        if (userId != null) {

            // Локаль
            Preference localePreference = preferenceBean.getPreference(userId, GLOBAL_PAGE, LOCALE_KEY);
            userLocale = localePreference != null ? new Locale(localePreference.getValue()) : null;
        }
        final IModel<Locale> localeModel = new Model<Locale>(userLocale != null ? userLocale : localeBean.getSystemLocale());

        Boolean copyUseDefaultAddressFlag = null;
        //Копирование
        if (copyUser) {
            // запомнить флаг установки адреса в поисковой строке прежде чем обнулить user id
            final Preference useDefaultAddressPreference = preferenceBean.getPreference(userId, GLOBAL_PAGE, IS_USE_DEFAULT_STATE_KEY);
            if (useDefaultAddressPreference != null) {
                copyUseDefaultAddressFlag = Boolean.valueOf(useDefaultAddressPreference.getValue());
            }

            userId = null;
            user.setId(null);
            user.setUserInfoObjectId(null);

            // заменить весь объект UserInfo
            user.setUserInfo(userInfoStrategy.newInstance());

            // очистить логин
            user.setLogin(null);

            for (UserOrganization userOrganization : user.getUserOrganizations()) {
                userOrganization.setId(null);
                userOrganization.setUserId(null);
            }

            for (UserGroup userGroup : user.getUserGroups()) {
                userGroup.setId(null);
                userGroup.setLogin(null);
            }
        }

        final User oldUser = (userId != null) ? CloneUtil.cloneObject(userModel.getObject()) : null;

        //Форма
        Form<User> form = new Form<User>("form");
        add(form);

        //Логин
        RequiredTextField<String> login = new RequiredTextField<String>("login", new PropertyModel<String>(userModel, "login"));
        login.setEnabled(userId == null);
        form.add(login);

        //Пароль
        PasswordTextField password = new PasswordTextField("password", new PropertyModel<String>(userModel, "newPassword"));
        password.setEnabled(userId != null);
        password.setRequired(false);
        form.add(password);

        //Информация о пользователе
        DomainObjectInputPanel userInfo = new DomainObjectInputPanel("user_info", userModel.getObject().getUserInfo(),
                "user_info", "UserInfoStrategy", null, null);
        form.add(userInfo);

        //Локаль
        form.add(new LocalePicker("locale", localeModel, false));

        //Группы привилегий
        CheckGroup<UserGroup> usergroups = new CheckGroup<UserGroup>("usergroups",
                new PropertyModel<Collection<UserGroup>>(userModel, "userGroups"));

        usergroups.add(new Check<UserGroup>("ADMINISTRATORS", getUserGroup(userModel.getObject(), ADMINISTRATORS)));
        usergroups.add(new Check<UserGroup>("EMPLOYEES", getUserGroup(userModel.getObject(), EMPLOYEES)));

        form.add(usergroups);

        //Сохранить
        IndicatingAjaxButton save = new IndicatingAjaxButton("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                User user = userModel.getObject();

                try {
                    //Валидация
                    if (UserEdit.this.validate(user)) {

                        //Сохранение пользователя
                        userBean.save(user, getTemplateSession());

                        //Локаль
                        preferenceBean.save(user.getId(), GLOBAL_PAGE, LOCALE_KEY, localeModel.getObject().getLanguage());

                        logBean.info(Module.NAME, UserEdit.class, User.class, null, user.getId(),
                                (user.getId() == null) ? Log.EVENT.CREATE : Log.EVENT.EDIT, getLogChanges(oldUser, user), null);

                        log.info("Пользователь сохранен: {}", user);
                        getSession().info(getString("info.saved"));
                        back(user.getId());
                    } else {
                        target.add(messages);
                    }
                } catch (Exception e) {
                    log.error("Ошибка сохранения пользователя", e);
                    error(getString("error.saved"));
                    target.add(messages);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        };
        form.add(save);

        //Отмена
        AjaxLink<Void> cancel = new AjaxLink<Void>("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                back(userModel.getObject().getId());
            }
        };
        form.add(cancel);
    }

    private boolean validate(User user) {
        boolean valid = true;

        //Уникальность логина
        if (user.getId() == null && !userBean.isUniqueLogin(user.getLogin())) {
            error(getString("error.login_not_unique"));
            valid = false;
        }

        return valid;
    }

    private IModel<UserGroup> getUserGroup(User user, UserGroup.GROUP_NAME group_name) {
        if (!user.getUserGroups().isEmpty()) {
            for (UserGroup userGroup : user.getUserGroups()) {
                if (userGroup.getGroupName().equals(group_name)) {
                    return new Model<UserGroup>(userGroup);
                }
            }
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName(group_name);
        return new Model<>(userGroup);
    }

    private List<LogChange> getLogChanges(User oldUser, User newUser) {
        List<LogChange> logChanges = new ArrayList<LogChange>();

        //логин
        if (newUser.getId() == null) {
            logChanges.add(new LogChange(getString("login"), null, newUser.getLogin()));
        }

        //пароль
        if (newUser.getNewPassword() != null) {
            logChanges.add(new LogChange(getString("password"), oldUser.getPassword(),
                    DigestUtils.md5Hex(newUser.getNewPassword())));
        }

        //информация о пользователе
        List<LogChange> userInfoLogChanges = logBean.getLogChanges(userInfoStrategy,
                oldUser != null ? oldUser.getUserInfo() : null, newUser.getUserInfo());

        logChanges.addAll(userInfoLogChanges);

        //группы привилегий
        if (oldUser == null) {
            for (UserGroup ng : newUser.getUserGroups()) {
                logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
            }
        } else {
            for (UserGroup og : oldUser.getUserGroups()) { //deleted group
                boolean deleted = true;

                for (UserGroup ng : newUser.getUserGroups()) {
                    if (ng.getGroupName().equals(og.getGroupName())) {
                        deleted = false;
                        break;
                    }
                }

                if (deleted) {
                    logChanges.add(new LogChange(getString("usergroup"), getString(og.getGroupName().name()), null));
                }
            }

            for (UserGroup ng : newUser.getUserGroups()) { //added group
                boolean added = true;

                for (UserGroup og : oldUser.getUserGroups()) {
                    if (og.getGroupName().equals(ng.getGroupName())) {
                        added = false;
                        break;
                    }
                }

                if (added) {
                    logChanges.add(new LogChange(getString("usergroup"), null, getString(ng.getGroupName().name())));
                }
            }
        }

        return logChanges;
    }

    private void back(Long userId) {
        if (userId != null) {
            PageParameters params = new PageParameters();
            params.set(UserList.SCROLL_PARAMETER, userId);
            setResponsePage(UserList.class, params);
        } else {
            setResponsePage(UserList.class);
        }
    }
}
