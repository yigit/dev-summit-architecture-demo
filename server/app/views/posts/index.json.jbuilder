json.array!(@posts) do |post|
  json.extract! post, :id, :text, :user_id, :client_id
  json.url post_url(post, format: :json)
end
