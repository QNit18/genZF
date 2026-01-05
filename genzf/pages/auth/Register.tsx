import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Mail, Lock, User, AlertCircle } from 'lucide-react';

export const Register: React.FC = () => {
  const { t, login } = useApp();
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Simulate API registration then auto-login
    setTimeout(() => {
      setIsLoading(false);
      // In a real app, this would create the user. 
      // For this demo, we just force a login with the "admin" credentials 
      // even though the user typed something else, so they get into the system.
      login('admin', 'admin'); 
      navigate('/portfolio');
    }, 1500);
  };

  return (
    <>
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-white">
          {t('auth.register_title')}
        </h2>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
          {t('auth.register_subtitle')}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5 mt-8">
        <Input 
          label={t('auth.name')}
          type="text" 
          placeholder="John Doe"
          required
          icon={<User size={16} />}
        />

        <Input 
          label={t('auth.email')}
          type="email" 
          placeholder="name@example.com"
          required
          icon={<Mail size={16} />}
        />
        
        <Input 
           label={t('auth.password')}
           type="password" 
           placeholder="••••••••"
           required
           icon={<Lock size={16} />}
        />

        <Input 
           label={t('auth.confirm_password')}
           type="password" 
           placeholder="••••••••"
           required
           icon={<Lock size={16} />}
        />

        <div className="flex items-center gap-2">
            <input type="checkbox" id="terms" className="rounded border-slate-300 dark:border-slate-700 text-blue-600 focus:ring-blue-500" required />
            <label htmlFor="terms" className="text-sm text-slate-600 dark:text-slate-400">
                I agree to the <a href="#" className="text-blue-600 hover:underline">Terms of Service</a>
            </label>
        </div>

        <Button fullWidth type="submit" disabled={isLoading}>
          {isLoading ? t('common.loading') : t('nav.register')}
        </Button>

        <p className="text-center text-sm text-slate-600 dark:text-slate-400">
          {t('auth.have_account')} {' '}
          <Link to="/auth/login" className="font-semibold text-blue-600 hover:text-blue-500 dark:text-blue-400">
            {t('nav.login')}
          </Link>
        </p>
      </form>
    </>
  );
};