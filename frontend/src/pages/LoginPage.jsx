import { useState } from 'react';
import api from '../api';
import { useNavigate, Link } from 'react-router-dom';
import { Lock, User } from 'lucide-react';

function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/auth/login', {
        username,
        password
      });

      // Save token and user details
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('apiKey', response.data.apiKey);
      localStorage.setItem('plan', response.data.plan);
      
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Invalid credentials');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900 text-slate-100">
      <div className="bg-slate-800 p-8 rounded-xl border border-slate-700 w-full max-w-md shadow-2xl">
        <h2 className="text-3xl font-bold mb-6 text-center bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
          Welcome Back
        </h2>
        
        {error && <div className="bg-red-500/20 text-red-400 p-3 rounded mb-4 text-center">{error}</div>}

        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="block text-sm font-medium mb-2 text-slate-400">Username</label>
            <div className="relative">
              <User className="absolute left-3 top-3 w-5 h-5 text-slate-500" />
              <input
                type="text"
                className="w-full bg-slate-700 border border-slate-600 rounded-lg py-2.5 pl-10 px-4 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                placeholder="Enter username"
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
                placeholder="Enter password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 py-3 rounded-lg font-bold transition-all transform hover:scale-[1.02]"
          >
            Login
          </button>
        </form>

        <p className="mt-6 text-center text-slate-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-blue-400 hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
