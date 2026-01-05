import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Mail, Lock, AlertCircle, Info } from 'lucide-react';

export const Login: React.FC = () => {
  const { t, login } = useApp();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const success = await login(formData.email, formData.password);
      
      if (success) {
        navigate('/portfolio');
      } else {
        setError('Invalid credentials. Please check your email and password.');
      }
    } catch (err) {
      setError('An unexpected error occurred.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  return (
    <>
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-white">
          {t('auth.login_title')}
        </h2>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
          {t('auth.login_subtitle')}
        </p>
      </div>

      <div className="bg-blue-50 dark:bg-blue-900/20 p-3 rounded-lg text-sm text-blue-700 dark:text-blue-300 flex items-start gap-2 mt-6">
        <Info size={16} className="mt-0.5 shrink-0" />
        <p>Demo Credentials: <strong>admin</strong> / <strong>admin</strong></p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 mt-6">
        {error && (
            <div className="bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 p-3 rounded-lg text-sm flex items-center gap-2 animate-in slide-in-from-top-2">
                <AlertCircle size={16} /> {error}
            </div>
        )}

        <Input 
          label={t('auth.email') + " (admin)"}
          name="email"
          type="text" 
          placeholder="admin"
          value={formData.email}
          onChange={handleChange}
          required
          icon={<Mail size={16} />}
        />
        
        <div className="space-y-1">
            <Input 
              label={t('auth.password') + " (admin)"}
              name="password"
              type="password" 
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
              required
              icon={<Lock size={16} />}
            />
            <div className="flex justify-end">
                <Link to="/auth/forgot-password" className="text-sm font-medium text-blue-600 hover:text-blue-500 dark:text-blue-400">
                    {t('auth.forgot_password')}
                </Link>
            </div>
        </div>

        <Button fullWidth type="submit" disabled={isLoading}>
          {isLoading ? (
            <span className="flex items-center gap-2">
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"/> 
                {t('common.loading')}
            </span>
          ) : t('nav.login')}
        </Button>

        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200 dark:border-slate-800"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="bg-slate-50 dark:bg-slate-950 px-2 text-slate-500">Or continue with</span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
            <Button type="button" variant="outline" className="w-full">Google</Button>
            <Button type="button" variant="outline" className="w-full">Github</Button>
        </div>

        <p className="text-center text-sm text-slate-600 dark:text-slate-400">
          {t('auth.no_account')} {' '}
          <Link to="/auth/register" className="font-semibold text-blue-600 hover:text-blue-500 dark:text-blue-400">
            {t('nav.register')}
          </Link>
        </p>
      </form>
    </>
  );
};