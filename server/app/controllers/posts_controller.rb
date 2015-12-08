#
# Copyright (C) 2015 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.
#
class PostsController < ApplicationController
  before_action :set_post, only: [:show, :edit, :update, :destroy]
  protect_from_forgery :except => :new_post

  # GET /posts
  # GET /posts.json
  def index
    @posts = Post.all
  end

  # GET /posts/1
  # GET /posts/1.json
  def show
  end

  # GET /posts/new
  def new
    @post = Post.new
  end

  # GET /posts/1/edit
  def edit
  end

  def feed
    if params[:since] != nil
      @posts = Post.where("created_at > ?", Time.at(params[:since].to_i / 1000).to_datetime)
    else
      @posts = Post.all
    end
    logger.debug @posts[0].user
    @users = @posts.map { |post| post.user }.uniq {|us| us.id}
    if @users.nil?
      @users = []
    end

    render :json => {
        :posts => @posts,
        :users => @users
    }
  end

  def user_feed
    @user = User.find_by_id(params[:user_id])
    if @user.nil?
      render json: "cannot find user", status: 401
    end
    if params[:since] != nil
      @posts = @user.posts.where("created_at > ?", Time.at(params[:since].to_i / 1000).to_datetime)
    else
      @posts = @user.posts
    end
    render :json => {
        :posts => @posts,
        :users => @users
    }
  end

  def new_post
    sleep(1.seconds)
    if error_before_saving_post
      x = 1/0
    end
    user_id = params[:user_id]
    @user = User.find(user_id)
    logger.debug "uid: #{@user.id}"
    if @user.nil?
      render json: "cannot find user", status: 401
      return
    end
    @post = Post.new({
                         :user_id => @user.id,
                         :client_id => params[:client_id],
                         :text => params[:text]
                     })
    begin
      if @post.save
        if error_after_saving_post
          x = 1/0
        end
        render :json => {
            :post => @post,
            :user => @user
        }
      else
        render json: @post.errors, status: :unprocessable_entity
      end
    rescue ActiveRecord::RecordNotUnique
      # looks like client is not aware that we've already saved this post. Find and return it kindly
      @post = Post.find_by_client_id_and_user_id(params[:client_id], user_id)
      if @post.nil?
        render json: {:error => "cannot create post"}, status: :unprocessable_entity
      else
        if error_after_saving_post
          x = 1/0
        end
        render :json => {
            :post => @post,
            :user => @user
        }
      end
    end
  end

  # POST /posts
  # POST /posts.json
  def create
    @post = Post.new(post_params)

    respond_to do |format|
      if @post.save
        format.html { redirect_to @post, notice: 'Post was successfully created.' }
        format.json { render :show, status: :created, location: @post }
      else
        format.html { render :new }
        format.json { render json: @post.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /posts/1
  # PATCH/PUT /posts/1.json
  def update
    respond_to do |format|
      if @post.update(post_params)
        format.html { redirect_to @post, notice: 'Post was successfully updated.' }
        format.json { render :show, status: :ok, location: @post }
      else
        format.html { render :edit }
        format.json { render json: @post.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /posts/1
  # DELETE /posts/1.json
  def destroy
    @post.destroy
    respond_to do |format|
      format.html { redirect_to posts_url, notice: 'Post was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_post
      @post = Post.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def post_params
      params.require(:post).permit(:text, :user_id, :client_id, :created)
    end

    def error_before_saving_post
      false
    end
    def error_after_saving_post
      false
    end
end
