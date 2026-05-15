import { useState, useEffect, useRef } from 'react';
import api from '../api';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Shield, Activity, Zap, Server, LogOut, Key, Send, FlaskConical } from 'lucide-react';

function DashboardPage() {
    const [stats, setStats] = useState(null);
    const [history, setHistory] = useState([]);
    const [requestLog, setRequestLog] = useState([]);
    const [isSending, setIsSending] = useState(false);
    const [isStressing, setIsStressing] = useState(false);
    const [stressResult, setStressResult] = useState(null);
    const logEndRef = useRef(null);
    const navigate = useNavigate();

    // Auto-scroll request log to bottom
    useEffect(() => {
        logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [requestLog]);

    const addLogEntry = (status, remaining, text) => {
        setRequestLog(prev => {
            const newLog = [...prev, {
                time: new Date().toLocaleTimeString(),
                status,
                remaining,
                text,
            }];
            if (newLog.length > 50) newLog.shift();
            return newLog;
        });
    };

    const sendTestRequest = async () => {
        if (isSending || isStressing) return;
        setIsSending(true);
        try {
            const response = await api.get('/weather/current', {
                headers: { 'X-API-KEY': stats.apiKey }
            });
            const remaining = response.headers['x-ratelimit-remaining'] ?? '?';
            addLogEntry(response.status, remaining, 'OK — Weather data returned');
        } catch (error) {
            const status = error.response?.status || 0;
            const remaining = error.response?.headers?.['x-ratelimit-remaining'] ?? '?';
            if (status === 429) {
                addLogEntry(429, remaining, 'Rate limit exceeded!');
            } else {
                addLogEntry(status, remaining, error.response?.data?.error || 'Request failed');
            }
        } finally {
            setIsSending(false);
        }
    };

    const runStressTest = async () => {
        if (isSending || isStressing) return;
        setIsStressing(true);
        setStressResult(null);
        const maxRequests = stats.plan === 'GOLD' ? 55 : 12;
        let succeeded = 0;

        for (let i = 0; i < maxRequests; i++) {
            try {
                const response = await api.get('/weather/current', {
                    headers: { 'X-API-KEY': stats.apiKey }
                });
                const remaining = response.headers['x-ratelimit-remaining'] ?? '?';
                succeeded++;
                addLogEntry(response.status, remaining, `Stress #${i + 1} — OK`);
            } catch (error) {
                const status = error.response?.status || 0;
                const remaining = error.response?.headers?.['x-ratelimit-remaining'] ?? '?';
                if (status === 429) {
                    addLogEntry(429, remaining, `Stress #${i + 1} — BLOCKED`);
                    setStressResult({ succeeded, blocked: i + 1 });
                    break;
                } else {
                    addLogEntry(status, remaining, `Stress #${i + 1} — Error`);
                }
            }
        }
        if (!stressResult) {
            setStressResult({ succeeded, blocked: null });
        }
        setIsStressing(false);
    };

    const getStatusColor = (status) => {
        if (status === 200) return 'text-green-400';
        if (status === 429) return 'text-red-400';
        return 'text-yellow-400';
    };

    const getStatusBg = (status) => {
        if (status === 200) return 'bg-green-500/10 border-green-500/30';
        if (status === 429) return 'bg-red-500/10 border-red-500/30';
        return 'bg-yellow-500/10 border-yellow-500/30';
    };

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        const fetchData = async () => {
            try {
                const response = await api.get('/admin/stats', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                const data = response.data;

                setStats(data);

                // Add to history graph
                setHistory(prev => {
                    const newPoint = {
                        time: new Date().toLocaleTimeString(),
                        usage: data.remaining
                    };
                    const newHistory = [...prev, newPoint];
                    if (newHistory.length > 20) newHistory.shift();
                    return newHistory;
                });

            } catch (error) {
                if (error.response?.status === 403 || error.response?.status === 401) {
                    localStorage.removeItem('token');
                    navigate('/login');
                }
            }
        };

        fetchData();
        const interval = setInterval(fetchData, 2000);
        return () => clearInterval(interval);
    }, [navigate]);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('apiKey');
        navigate('/login');
    };

    if (!stats) return <div className="min-h-screen bg-slate-900 flex items-center justify-center text-xl text-slate-300">Connecting to API...</div>;

    return (
        <div className="min-h-screen bg-slate-900 text-slate-100 p-8">
            <div className="max-w-6xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-10">
                    <div className="flex items-center gap-3">
                        <Shield className="w-10 h-10 text-blue-500" />
                        <div>
                            <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
                                API Dashboard
                            </h1>
                            <p className="text-slate-400 text-sm">Welcome, {stats.username}</p>
                        </div>
                    </div>
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-2 px-4 py-2 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors border border-slate-700"
                    >
                        <LogOut className="w-4 h-4" /> Logout
                    </button>
                </div>

                {/* API Key Banner */}
                <div className="bg-gradient-to-r from-blue-900/20 to-purple-900/20 border border-blue-500/30 p-4 rounded-xl mb-8 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <Key className="text-blue-400" />
                        <span className="font-mono text-slate-300">Your API Key:</span>
                        <code className="bg-slate-950 px-3 py-1 rounded text-blue-300 font-mono select-all">
                            {stats.apiKey}
                        </code>
                    </div>
                    <div className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
                        {stats.plan} PLAN
                    </div>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">

                    {/* Usage Card */}
                    <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg relative overflow-hidden">
                        <div className="flex justify-between items-center mb-4 relative z-10">
                            <h2 className="text-xl font-semibold text-slate-300">Token Bucket</h2>
                            {stats.plan === 'GOLD' ? <Server className="text-purple-400" /> : <Zap className="text-yellow-400" />}
                        </div>

                        <div className="text-4xl font-bold mb-2 relative z-10">
                            {stats.remaining} <span className="text-sm text-slate-500">/ {stats.limit}</span>
                        </div>

                        <div className="w-full bg-slate-700 rounded-full h-2.5 relative z-10">
                            <div
                                className={`h-2.5 rounded-full transition-all duration-500 ${stats.remaining < stats.limit * 0.2 ? 'bg-red-500' : 'bg-blue-600'
                                    }`}
                                style={{ width: `${(stats.remaining / stats.limit) * 100}%` }}
                            ></div>
                        </div>

                        {/* Background Glow */}
                        <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/10 rounded-full blur-3xl -mr-10 -mt-10"></div>
                    </div>

                    {/* Plan Details Card */}
                    <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-semibold text-slate-300">Plan Details</h2>
                            <Activity className="text-green-400" />
                        </div>
                        <div className="space-y-4">
                            <div className="flex justify-between border-b border-slate-700 pb-2">
                                <span className="text-slate-400">Rate Limit</span>
                                <span className="font-bold">{stats.plan === 'GOLD' ? '50 req/sec' : '10 req/sec'}</span>
                            </div>
                            <div className="flex justify-between border-b border-slate-700 pb-2">
                                <span className="text-slate-400">Refill Rate</span>
                                <span className="font-bold">{stats.plan === 'GOLD' ? '5 tokens/sec' : '1 token/sec'}</span>
                            </div>
                            <div className="flex justify-between">
                                <span className="text-slate-400">Status</span>
                                <span className="text-green-400 font-bold">Active</span>
                            </div>
                        </div>
                    </div>

                </div>

                {/* Test API Panel */}
                <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg mb-8">
                    <div className="flex items-center justify-between mb-5">
                        <div className="flex items-center gap-2">
                            <FlaskConical className="text-purple-400" />
                            <h2 className="text-xl font-semibold">Test API</h2>
                        </div>
                        <div className="flex gap-3">
                            <button
                                onClick={sendTestRequest}
                                disabled={isSending || isStressing}
                                className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 disabled:cursor-not-allowed rounded-lg transition-colors text-sm font-medium"
                            >
                                <Send className="w-4 h-4" />
                                {isSending ? 'Sending...' : 'Send Request'}
                            </button>
                            <button
                                onClick={runStressTest}
                                disabled={isSending || isStressing}
                                className="flex items-center gap-2 px-4 py-2 bg-purple-600 hover:bg-purple-700 disabled:bg-slate-600 disabled:cursor-not-allowed rounded-lg transition-colors text-sm font-medium"
                            >
                                <Zap className="w-4 h-4" />
                                {isStressing ? 'Running...' : 'Stress Test'}
                            </button>
                        </div>
                    </div>

                    {/* Stress Test Result Banner */}
                    {stressResult && (
                        <div className={`p-3 rounded-lg mb-4 text-sm font-medium border ${stressResult.blocked ? 'bg-red-500/10 border-red-500/30 text-red-300' : 'bg-green-500/10 border-green-500/30 text-green-300'}`}>
                            {stressResult.blocked
                                ? `🛑 Rate limited! ${stressResult.succeeded} requests succeeded before being blocked on request #${stressResult.blocked}.`
                                : `✅ All ${stressResult.succeeded} requests succeeded (limit not reached).`
                            }
                        </div>
                    )}

                    {/* Request History Log */}
                    <div className="bg-slate-900 rounded-lg border border-slate-700 max-h-48 overflow-y-auto">
                        {requestLog.length === 0 ? (
                            <div className="p-4 text-center text-slate-500 text-sm">
                                No requests sent yet. Click <span className="text-blue-400">Send Request</span> or <span className="text-purple-400">Stress Test</span> to begin.
                            </div>
                        ) : (
                            <div className="divide-y divide-slate-800">
                                {requestLog.map((entry, i) => (
                                    <div key={i} className={`flex items-center gap-3 px-4 py-2 text-sm border-l-2 ${getStatusBg(entry.status)}`}>
                                        <span className="text-slate-500 font-mono text-xs w-20 shrink-0">{entry.time}</span>
                                        <span className={`font-bold font-mono w-10 shrink-0 ${getStatusColor(entry.status)}`}>{entry.status}</span>
                                        <span className="text-slate-400 truncate flex-1">{entry.text}</span>
                                        <span className="text-slate-500 text-xs font-mono shrink-0">rem: {entry.remaining}</span>
                                    </div>
                                ))}
                                <div ref={logEndRef} />
                            </div>
                        )}
                    </div>
                </div>

                {/* Real-time Chart */}
                <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-lg">
                    <div className="flex items-center gap-2 mb-6">
                        <Activity className="text-blue-400" />
                        <h2 className="text-xl font-semibold">Real-time Usage</h2>
                    </div>
                    <div className="h-[300px] w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={history}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                                <XAxis dataKey="time" stroke="#94a3b8" />
                                <YAxis stroke="#94a3b8" domain={[0, stats.limit]} />
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#1e293b', border: 'none' }}
                                    itemStyle={{ color: '#fff' }}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="usage"
                                    stroke="#3b82f6"
                                    strokeWidth={3}
                                    dot={false}
                                    name="Available Tokens"
                                    isAnimationActive={false}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;
