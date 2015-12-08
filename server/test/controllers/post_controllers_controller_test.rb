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
require 'test_helper'

class PostControllersControllerTest < ActionController::TestCase
  setup do
    @post_controller = post_controllers(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:post_controllers)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create post_controller" do
    assert_difference('PostController.count') do
      post :create, post_controller: {  }
    end

    assert_redirected_to post_controller_path(assigns(:post_controller))
  end

  test "should show post_controller" do
    get :show, id: @post_controller
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @post_controller
    assert_response :success
  end

  test "should update post_controller" do
    patch :update, id: @post_controller, post_controller: {  }
    assert_redirected_to post_controller_path(assigns(:post_controller))
  end

  test "should destroy post_controller" do
    assert_difference('PostController.count', -1) do
      delete :destroy, id: @post_controller
    end

    assert_redirected_to post_controllers_path
  end
end
