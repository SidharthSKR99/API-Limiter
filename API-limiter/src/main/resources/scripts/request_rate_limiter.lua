-- keys[1] is the unique ID for the user
-- argv[1] is the Refill Rate (tokens per second)
-- argv[2] is the Burst Capacity (max tokens)
-- argv[3] is the Requested Tokens (usually 1)

local tokens_key = KEYS[1] .. ":tokens"
local timestamp_key = KEYS[1] .. ":ts"

local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = redis.call('TIME')[1]
local requested = tonumber(ARGV[3])

local fill_time = capacity / rate
local ttl = math.floor(fill_time * 2)

-- Fetch current state
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
    last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
    last_refreshed = 0
end

-- Refill bucket
local delta = math.max(0, now - last_refreshed)
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))

-- FIX: Use 0 instead of false
local allowed = 0

-- Check and decrement
if filled_tokens >= requested then
    -- FIX: Use 1 instead of true
    allowed = 1
    filled_tokens = filled_tokens - requested
end

-- Save new state
redis.call("setex", tokens_key, ttl, filled_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- Return [1 if allowed, tokens remaining]
return { allowed, filled_tokens }