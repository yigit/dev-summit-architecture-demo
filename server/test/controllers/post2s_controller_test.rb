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

class Post2sControllerTest < ActionController::TestCase
  setup do
    @post2 = post2s(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:post2s)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create post2" do
    assert_difference('Post2.count') do
      post :create, post2: { created: @post2.created, text: @post2.text }
    end

    assert_redirected_to post2_path(assigns(:post2))
  end

  test "should show post2" do
    get :show, id: @post2
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @post2
    assert_response :success
  end

  test "should update post2" do
    patch :update, id: @post2, post2: { created: @post2.created, text: @post2.text }
    assert_redirected_to post2_path(assigns(:post2))
  end

  test "should destroy post2" do
    assert_difference('Post2.count', -1) do
      delete :destroy, id: @post2
    end

    assert_redirected_to post2s_path
  end
end
