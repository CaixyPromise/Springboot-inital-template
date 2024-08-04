package com.caixy.adminSystem.model.dto.oauth.github;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * Github返回的用户信息载体
 *
 * @Author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.dto.oauth.github.GithubUserProfileDTO
 * @since 2024/8/3 上午1:11
 */
@Data
public class GithubUserProfileDTO implements Serializable
{
    /**
     * 获取用户信息失败时返回的错误信息，当成功时此处为空
     */
    private String message;
    /**
     * 用户登录名，通常用作GitHub的用户名
     */
    @SerializedName("login")
    private String loginUserName;

    /**
     * github用户的唯一标识符
     */
    private Long id;

    /**
     * 用户的节点标识符，用于GitHub的内部跟踪
     */
    @SerializedName("node_id")
    private String nodeId;

    /**
     * 用户的头像链接地址
     */
    @SerializedName("avatar_url")
    private String avatarUrl;

    /**
     * 用户的Gravatar头像标识符，若未设置则为空字符串
     */
    @SerializedName("gravatar_id")
    private String gravatarId;

    /**
     * 指向用户GitHub主页的URL
     */
    @SerializedName("url")
    private String userProfileUrl;

    /**
     * 用户的HTML URL，通常指向GitHub的公开主页
     */
    @SerializedName("html_url")
    private String htmlUrl;

    /**
     * 用户的关注者列表URL
     */
    @SerializedName("followers_url")
    private String followersUrl;

    /**
     * 用户正在关注的用户列表URL
     */
    @SerializedName("following_url")
    private String followingUrl;

    /**
     * 用户的Gists列表URL
     */
    @SerializedName("gists_url")
    private String gistsUrl;

    /**
     * 用户星标的仓库列表URL
     */
    @SerializedName("starred_url")
    private String starredUrl;

    /**
     * 用户订阅的仓库列表URL
     */
    @SerializedName("subscriptions_url")
    private String subscriptionsUrl;

    /**
     * 用户所属的组织列表URL
     */
    @SerializedName("organizations_url")
    private String organizationsUrl;

    /**
     * 用户的仓库列表URL
     */
    @SerializedName("repos_url")
    private String reposUrl;

    /**
     * 用户参与的事件列表URL
     */
    @SerializedName("events_url")
    private String eventsUrl;

    /**
     * 用户接收的事件列表URL
     */
    @SerializedName("received_events_url")
    private String receivedEventsUrl;

    /**
     * 用户类型，例如“User”或“Organization”
     */
    private String type;

    /**
     * 用户是否为GitHub站点管理员
     */
    @SerializedName("site_admin")
    private boolean siteAdmin;

    /**
     * 用户的公开名称
     */
    private String name;

    /**
     * 用户所在的公司名称
     */
    private String company;

    /**
     * 用户的博客地址
     */
    private String blog;

    /**
     * 用户所在的地理位置
     */
    private String location;

    /**
     * 用户的电子邮件地址
     */
    private String email;

    /**
     * 用户是否可雇佣，通常用于表示用户是否开放工作机会
     */
    private Boolean hireable;

    /**
     * 用户的个人简介
     */
    private String bio;

    /**
     * 用户的Twitter用户名
     */
    @SerializedName("twitter_username")
    private String twitterUsername;

    /**
     * 用户的通知邮件地址，用于GitHub发送通知
     */
    @SerializedName("notification_email")
    private String notificationEmail;

    /**
     * 用户的公开仓库数量
     */
    @SerializedName("public_repos")
    private int publicRepos;

    /**
     * 用户的公开Gists数量
     */
    @SerializedName("public_gists")
    private int publicGists;

    /**
     * 用户的关注者数量
     */
    private int followers;

    /**
     * 用户正在关注的人数
     */
    private int following;

    /**
     * 用户账户的创建时间
     */
    @SerializedName("created_at")
    private String createdAt;

    /**
     * 用户信息的最后更新时间
     */
    @SerializedName("updated_at")
    private String updatedAt;

    private static final long serialVersionUID = 1L;
}
