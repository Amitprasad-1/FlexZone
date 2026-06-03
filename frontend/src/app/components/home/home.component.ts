import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './home.component.html'
})
export class HomeComponent {
  constructor(public authService: AuthService) {}

  // Public interactive BMI Calculator
  heightCm = 175;
  weightKg = 70;
  calculatedBmi: number | null = null;
  bmiCategory = '';
  bmiSuggestion = '';

  calculateBmiValue(): void {
    if (this.heightCm <= 0 || this.weightKg <= 0) return;
    const heightM = this.heightCm / 100;
    const bmiVal = this.weightKg / (heightM * heightM);
    this.calculatedBmi = parseFloat(bmiVal.toFixed(1));
    
    if (this.calculatedBmi < 18.5) {
      this.bmiCategory = 'Underweight';
      this.bmiSuggestion = 'A high-calorie muscle gain plan with clean carbs and whey protein is recommended. Join us to start building healthy mass!';
    } else if (this.calculatedBmi >= 18.5 && this.calculatedBmi < 25) {
      this.bmiCategory = 'Normal Weight';
      this.bmiSuggestion = 'Outstanding shape! Focus on strength training and conditioning to maintain muscle tone and stamina.';
    } else if (this.calculatedBmi >= 25 && this.calculatedBmi < 30) {
      this.bmiCategory = 'Overweight';
      this.bmiSuggestion = 'A structured calorie deficit combined with high-intensity interval training (HIIT) and weightlifting is ideal.';
    } else {
      this.bmiCategory = 'Obese';
      this.bmiSuggestion = 'Let us help you! A dedicated trainer will design a safe, highly effective cardio and low-impact strength schedule.';
    }
  }

  // Local data for gym plans
  plans = [
    {
      id: 1,
      title: 'Silver Package',
      price: 999,
      duration: 'Monthly',
      popular: false,
      features: [
        'Gym Floor Access (05:00 AM - 10:00 PM)',
        'Full Strength & Cardio Equipment',
        'Standard Locker Room Access',
        'Free Initial Fitness Evaluation',
        'General Trainer Assistance'
      ]
    },
    {
      id: 2,
      title: 'Gold Package',
      price: 2499,
      duration: 'Quarterly',
      popular: true,
      features: [
        'All Silver Package Features Included',
        '2 Days/Week Personal Trainer Session',
        'Customized Diet & Workout Charts',
        'Zumba & Aerobics Class Bookings',
        'Body Composition Tracking'
      ]
    },
    {
      id: 3,
      title: 'Platinum Package',
      price: 8999,
      duration: 'Yearly',
      popular: false,
      features: [
        'Unrestricted 24/7 Floor & Gym Access',
        '3 Days/Week Personal Trainer Session',
        'Unlimited Specialized Class Bookings',
        'Welcome Kit (FlexFit T-Shirt + Shaker)',
        'Monthly Health Review & Adjustments'
      ]
    }
  ];

  // Local data for trainers
  trainers = [
    {
      name: 'Vikram Singh',
      role: 'Head Strength Coach',
      experience: '8+ Years',
      certs: 'ISSA Certified Personal Trainer, Advanced Powerlifting Coach',
      bio: 'Vikram specializes in heavy lifts, core strength conditioning, and competitive bodybuilding preparation.',
      bgGradient: 'from-amber-500/20 to-orange-500/20 shadow-orange-500/10',
      photoUrl: 'trainer1.png'
    },
    {
      name: 'Pooja Sharma',
      role: 'Yoga & Zumba Instructor',
      experience: '5+ Years',
      certs: 'RYT 200 Yoga Alliance Certified, Zumba® Licensed Instructor',
      bio: 'Pooja blends holistic flexibility workouts with high-energy aerobics, perfect for cardio endurance and weight loss.',
      bgGradient: 'from-purple-500/20 to-pink-500/20 shadow-pink-500/10',
      photoUrl: 'trainer2.png'
    },
    {
      name: 'Rajesh Sen',
      role: 'Nutrition & Transformation Expert',
      experience: '6+ Years',
      certs: 'ACE Certified Personal Trainer, Clinical Nutritionist Diploma',
      bio: 'Rajesh focuses on functional fitness, medical lifestyle corrections, and custom meal planning.',
      bgGradient: 'from-emerald-500/20 to-teal-500/20 shadow-emerald-500/10',
      photoUrl: 'trainer3.png'
    }
  ];

  // Gym FAQ
  faqs = [
    {
      q: 'Do you offer a free trial before joining?',
      a: 'Yes, we offer a complimentary 2-day pass for Asansol residents. Simply drop by our reception and present a valid ID to start.',
      open: false
    },
    {
      q: 'How does the slot booking system work?',
      a: 'To prevent overcrowding, members can book their preferred hourly time-slots or group classes (Zumba, Yoga) in their panel. Each slot has strict capacity validation.',
      open: false
    },
    {
      q: 'Are personal trainers included in the plan?',
      a: 'General assistance is free for everyone. Personalized one-on-one fitness training is included in our Gold (Quarterly) and Platinum (Yearly) subscription packages.',
      open: false
    },
    {
      q: 'Can I purchase supplements directly from the gym?',
      a: 'Yes! We run an Integrated E-Shop selling premium whey protein, shakers, workout apparel, and gym accessories. You can order online and check out.',
      open: false
    }
  ];

  toggleFaq(index: number): void {
    this.faqs[index].open = !this.faqs[index].open;
  }
}
