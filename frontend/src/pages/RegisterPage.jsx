import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { Lock, User, Crown } from 'lucide-react';

function RegisterPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [plan, setPlan] = useState('FREE');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8080/api/auth/register', {
                username,
                password,
                plan
            });
            navigate('/login');
        } catch (err) {
            setError(err.response?.data || 'Registration failed');
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-900 text-slate-100">
            <div className="bg-slate-800 p-8 rounded-xl border border-slate-700 w-full max-w-md shadow-2xl">
                <h2 className="text-3xl font-bold mb-6 text-center bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
                    Create Account
                </h2>

                {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4 text-center">{error}</div>}

                <form onSubmit={handleRegister} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium mb-2 text-slate-400">Username</label>
                        <div className="relative">
                            <User className="absolute left-3 top-3 w-5 h-5 text-slate-500" />
                            <input
                                type="text"
                                className="w-full bg-slate-700 border border-slate-600 rounded-lg py-2.5 pl-10 px-4 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                                placeholder="Choose a username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-2 text-slate-400">Password</label>
                        <div className="relative">
                            <Lock className="absolute left-3 top-3 w-5 h-5 text-slate-500" />
                            <input
                                type="password"
                                className="w-full bg-slate-700 border border-slate-600 rounded-lg py-2.5 pl-10 px-4 focus:outline-none focus:ring-2 focus:ring-purple-500 transition-all"
                                placeholder="Choose a password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-2 text-slate-400">Select Plan</label>
                        <div className="grid grid-cols-2 gap-4">
                            <div
                                className={`p-4 border rounded-xl cursor-pointer transition-all ${plan === 'FREE' ? 'border-blue-500 bg-blue-500/10' : 'border-slate-600 bg-slate-700'
                                    }`}
                                onClick={() => setPlan('FREE')}
                            >
                                <div className="font-bold text-lg mb-1">Free</div>
                                <div className="text-xs text-slate-400">10 req/sec</div>
                            </div>
                            <div
                                className={`p-4 border rounded-xl cursor-pointer transition-all ${plan === 'GOLD' ? 'border-purple-500 bg-purple-500/10' : 'border-slate-600 bg-slate-700'
                                    }`}
                                onClick={() => setPlan('GOLD')}
                            >
                                <div className="flex items-center gap-2 font-bold text-lg mb-1 text-purple-400">
                                    Gold <Crown className="w-4 h-4" />
                                </div>
                                <div className="text-xs text-slate-400">50 req/sec</div>
                            </div>
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 py-3 rounded-lg font-bold transition-all transform hover:scale-[1.02]"
                    >
                        Register
                    </button>
                </form>

                <p className="mt-6 text-center text-slate-400">
                    Already have an account?{' '}
                    <Link to="/login" className="text-blue-400 hover:underline">
                        Login
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default RegisterPage;
