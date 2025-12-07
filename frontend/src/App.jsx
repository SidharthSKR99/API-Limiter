import { useState, useEffect } from 'react';
import axios from 'axios';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Shield, Activity, Zap, Server } from 'lucide-react';

function App() {
  const [stats, setStats] = useState(null);
  const [history, setHistory] = useState([]);

  // The "Poller" - Fetches data every 2 seconds
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/admin/stats');
        const data = response.data;

        setStats(data);

        // Add to history graph (keep last 20 points)
        setHistory(prev => {
          const newPoint = {
            time: new Date().toLocaleTimeString(),
            free: data.free_plan_remaining,
            gold: data.gold_plan_remaining
          };
          const newHistory = [...prev, newPoint];
          if (newHistory.length > 20) newHistory.shift();
          return newHistory;
        });

      } catch (error) {
        console.error("Error connecting to Backend:", error);
      }
    };

    fetchData(); // Fetch immediately
    const interval = setInterval(fetchData, 2000); // Then every 2s
    return () => clearInterval(interval);
  }, []);

  if (!stats) return <div className="text-center mt-20 text-xl">Connecting to Redis...</div>;

  return (
    <div className="min-h-screen p-8 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <Shield className="w-10 h-10 text-blue-500" />
        <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
          API Guardian Dashboard
        </h1>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">

        {/* Free Plan Card */}
        <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-slate-300">Free Plan</h2>
            <Zap className="text-yellow-400" />
          </div>
          <div className="text-4xl font-bold mb-2">
            {stats.free_plan_remaining} <span className="text-sm text-slate-500">/ {stats.free_plan_limit} tokens</span>
          </div>
          {/* Progress Bar */}
          <div className="w-full bg-slate-700 rounded-full h-2.5">
            <div
              className="bg-blue-600 h-2.5 rounded-full transition-all duration-500"
              style={{ width: `${(stats.free_plan_remaining / stats.free_plan_limit) * 100}%` }}
            ></div>
          </div>
        </div>

        {/* Gold Plan Card */}
        <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-slate-300">Gold Plan</h2>
            <Server className="text-purple-400" />
          </div>
          <div className="text-4xl font-bold mb-2">
            {stats.gold_plan_remaining} <span className="text-sm text-slate-500">/ {stats.gold_plan_limit} tokens</span>
          </div>
          <div className="w-full bg-slate-700 rounded-full h-2.5">
            <div
              className="bg-purple-600 h-2.5 rounded-full transition-all duration-500"
              style={{ width: `${(stats.gold_plan_remaining / stats.gold_plan_limit) * 100}%` }}
            ></div>
          </div>
        </div>
      </div>

      {/* Real-time Chart */}
      <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg">
        <div className="flex items-center gap-2 mb-6">
          <Activity className="text-green-400" />
          <h2 className="text-xl font-semibold">Real-time Token Usage</h2>
        </div>
        <div className="h-[300px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={history}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="time" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip
                contentStyle={{ backgroundColor: '#1e293b', border: 'none' }}
                itemStyle={{ color: '#fff' }}
              />
              <Line type="monotone" dataKey="free" stroke="#3b82f6" strokeWidth={3} dot={false} name="Free Plan" />
              <Line type="monotone" dataKey="gold" stroke="#a855f7" strokeWidth={3} dot={false} name="Gold Plan" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}

export default App;
