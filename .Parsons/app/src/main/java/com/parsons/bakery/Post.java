package com.parsons.bakery;

public class Post {
    String Response;
    String id;
    String name;
    String img;
    String category;
    String description;
    String req;
    String type;
    String options;
    String item;
    String title;
    String level;
    String inner_category;
    String has_levels;
    String use_inner;
    String username;
    String hashed_pass;
    String number;
    String order_of_options;
    String is_required;
    String is_baker;
    String order_placed;
    String time_placed;
    String needs_verification;
    String unique_id;
    String recipient;
    String sender;
    String history;
    String name1;
    public Post(String name, String img, String category, String description, String req) {
        this.name = name;
        this.img = img;
        this.category = category;
        this.description = description;
        this.req = req;
    }
    public Post() {

    }
    public String getName1() {
        return name1;
    }
    public String getSender() {
        return sender;
    }
    public String getRecipient() {
        return recipient;
    }
    public String getHistory() {
        return history;
    }
    public String getResponse() {
        return Response;
    }
    public String getName() {
        return name;
    }
    public String getImg() {
        return img;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }
    public String getReq() {
        return req;
    }
    public String getItem() {
        return item;
    }
    public String getOptions() {
        return options;
    }
    public String getType() {
        return type;
    }
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getLevel() {
        return level;
    }
    public String getInner_category() {
        return inner_category;
    }
    public String getHas_levels() {
        return has_levels;
    }
    public String getUse_inner() {
        return use_inner;
    }
    public String getUsername() {
        return username;
    }
    public String getHashed_pass() {
        return hashed_pass;
    }
    public String getNumber() {
        return number;
    }
    public String getOrder_of_options() {
        return order_of_options;
    }
    public String getIs_required() {
        return is_required;
    }
    public String getIs_baker() {
        return is_baker;
    }
    public String getNeeds_verification() {
        return needs_verification;
    }
    public String getOrder_placed() {
        return order_placed;
    }
    public String getTime_placed() {
        return time_placed;
    }
    public String getUnique_id() {
        return unique_id;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public void setHistory(String history) {
        this.history = history;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }
    public void setResponse(String response) {
        Response = response;
    }
    public void setNeeds_verification(String needs_verification) {
        this.needs_verification = needs_verification;
    }
    public void setOrder_placed(String order_placed) {
        this.order_placed = order_placed;
    }
    public void setTime_placed(String time_placed) {
        this.time_placed = time_placed;
    }
    public void setIs_baker(String is_baker) {
        this.is_baker = is_baker;
    }
    public void setIs_required(String is_required) {
        this.is_required = is_required;
    }
    public void setOrder_of_options(String order_of_options) {
        this.order_of_options = order_of_options;
    }
    public void setHashed_pass(String hashed_pass) {
        this.hashed_pass = hashed_pass;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setImg(String img) {
        this.img = img;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setReq(String req) {
        this.req = req;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public void setOptions(String options) {
        this.options = options;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    public void setInner_category(String inner_category) {
        this.inner_category = inner_category;
    }
    public void setHas_levels(String has_levels) {
        this.has_levels = has_levels;
    }
    public void setUse_inner(String use_inner) {
        this.use_inner = use_inner;
    }
}
