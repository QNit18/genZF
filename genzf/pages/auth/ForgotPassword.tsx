
import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Mail, ArrowLeft, CheckCircle2 } from 'lucide-react';

export const ForgotPassword: React.FC = () => {
  const { t } = useApp();
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Simulate API
    setTimeout(() => {
      setIsLoading(false);
      setIsSent(true);
    }, 1500);
  };

  if (isSent) {
      return (
          <div className="text-center space-y-4 animate-in fade-in zoom-in-95 duration-300">
              <div className="mx-auto w-16 h-16 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400 rounded-full flex items-center justify-center">
                  <CheckCircle2 size={32} />
              </div>
              <h2 className="text-2xl font-bold">{t('auth.sent_title')}</h2>
              <p className="text-slate-600 dark:text-slate-400">
                  {t('auth.sent_desc')}
              </p>
              <div className="pt-4">
                <Link to="/auth/login" className="text-blue-600 hover:underline font-medium">
                    {t('auth.back_to_login')}
                </Link>
              </div>
          </div>
      )
  }

  return (
    <>
      <div className="text-center">
        <h2 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-white">
          {t('auth.reset_password')}
        </h2>
        <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
          {t('auth.sent_desc')}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6 mt-8">
        <Input 
          label={t('auth.email')}
          type="email" 
          placeholder="name@example.com"
          required
          icon={<Mail size={16} />}
        />

        <Button fullWidth type="submit" disabled={isLoading}>
          {isLoading ? t('common.loading') : t('common.submit')}
        </Button>

        <p className="text-center text-sm">
          <Link to="/auth/login" className="flex items-center justify-center gap-2 text-slate-600 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-200 transition-colors">
            <ArrowLeft size={16} /> {t('auth.back_to_login')}
          </Link>
        </p>
      </form>
    </>
  );
};
