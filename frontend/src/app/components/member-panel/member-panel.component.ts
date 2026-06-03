import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MemberService } from '../../services/member.service';
import { EShopService } from '../../services/eshop.service';

@Component({
  selector: 'app-member-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './member-panel.component.html'
})
export class MemberPanelComponent implements OnInit {
  profile: any = null;
  bmiHistory: any[] = [];
  orders: any[] = [];

  // Active view tab: 'metrics', 'workout', 'diet'
  activeTab = 'metrics';

  // BMI Input
  heightCm = 170;
  weightKg = 70;
  calculatedBmi: number | null = null;
  bmiCategory = '';

  loading = true;
  errorMessage = '';
  successMessage = '';

  // Realistic workout routines based on trainer assignment
  workoutRoutine: any = {
    general: [
      { day: 'Monday', focus: 'Chest & Triceps', exercises: [
        { name: 'Flat Barbell Bench Press', sets: '4', reps: '10-12', weight: 'Bar + 15kg each side' },
        { name: 'Incline Dumbbell Press', sets: '3', reps: '12', weight: '12.5kg each' },
        { name: 'Cable Chest Flyes', sets: '3', reps: '15', weight: '15kg each side' },
        { name: 'Tricep Overhead Extension', sets: '3', reps: '12', weight: '15kg' },
        { name: 'Tricep Rope Pushdowns', sets: '4', reps: '15', weight: '20kg' }
      ]},
      { day: 'Tuesday', focus: 'Back & Biceps', exercises: [
        { name: 'Wide Lat Pulldowns', sets: '4', reps: '10', weight: '45kg' },
        { name: 'One-Arm Dumbbell Rows', sets: '3', reps: '12', weight: '17.5kg' },
        { name: 'Seated Cable Row (V-Bar)', sets: '3', reps: '12', weight: '40kg' },
        { name: 'Barbell Bicep Curls', sets: '4', reps: '10', weight: '20kg' },
        { name: 'Incline Dumbbell Curls', sets: '3', reps: '12', weight: '10kg each' }
      ]},
      { day: 'Wednesday', focus: 'Legs & Calves', exercises: [
        { name: 'Barbell Back Squats', sets: '4', reps: '8-10', weight: 'Bar + 20kg each side' },
        { name: 'Leg Press (Wide stance)', sets: '3', reps: '12', weight: '120kg' },
        { name: 'Lying Leg Curls', sets: '3', reps: '15', weight: '30kg' },
        { name: 'Seated Calf Raises', sets: '4', reps: '20', weight: '25kg' }
      ]},
      { day: 'Thursday', focus: 'Shoulders & Abs', exercises: [
        { name: 'Seated Dumbbell Shoulder Press', sets: '4', reps: '10', weight: '15kg each' },
        { name: 'Dumbbell Lateral Raises', sets: '4', reps: '15', weight: '7.5kg each' },
        { name: 'Cable Face Pulls', sets: '3', reps: '15', weight: '17.5kg' },
        { name: 'Hanging Leg Raises', sets: '3', reps: '15', weight: 'Bodyweight' }
      ]},
      { day: 'Friday', focus: 'Arms & Conditioning', exercises: [
        { name: 'Close-Grip Bench Press', sets: '3', reps: '10', weight: '40kg' },
        { name: 'Preacher Bench Bicep Curls', sets: '3', reps: '12', weight: '25kg' },
        { name: 'Dumbbell Hammer Curls', sets: '3', reps: '12', weight: '12.5kg each' },
        { name: 'Kettlebell Swings', sets: '4', reps: '20', weight: '16kg' }
      ]},
      { day: 'Saturday', focus: 'Cardio & Recovery', exercises: [
        { name: 'Treadmill Incline Walk', sets: '1', reps: '25 mins', weight: 'Speed 5.5, Incline 6.0' },
        { name: 'Elliptical Cross Trainer', sets: '1', reps: '15 mins', weight: 'Level 8' },
        { name: 'Full Body Stretching', sets: '1', reps: '15 mins', weight: 'N/A' }
      ]},
      { day: 'Sunday', focus: 'Rest & Repair', exercises: [
        { name: 'Complete Rest Day', sets: 'N/A', reps: 'N/A', weight: 'N/A' }
      ]}
    ],
    custom: [
      { day: 'Monday', focus: 'Push Day (Chest, Shoulders, Triceps)', exercises: [
        { name: 'Incline Barbell Bench Press', sets: '4', reps: '8-10', weight: 'Bar + 25kg each side' },
        { name: 'Flat Dumbbell Press', sets: '3', reps: '10', weight: '25kg each' },
        { name: 'Overhead Standing Military Press', sets: '3', reps: '8', weight: '35kg' },
        { name: 'Dumbbell Lateral Raises', sets: '4', reps: '12-15', weight: '10kg each' },
        { name: 'Skullcrushers (EZ Bar)', sets: '3', reps: '12', weight: '22.5kg' }
      ]},
      { day: 'Tuesday', focus: 'Pull Day (Back, Rear Delts, Biceps)', exercises: [
        { name: 'Weighted Pull-Ups', sets: '4', reps: '6-8', weight: 'Bodyweight + 10kg' },
        { name: 'T-Bar Rows (Chest supported)', sets: '3', reps: '10', weight: '45kg' },
        { name: 'Reverse Lat Pulldowns', sets: '3', reps: '12', weight: '55kg' },
        { name: 'Incline Dumbbell Curls', sets: '3', reps: '12', weight: '12.5kg each' },
        { name: 'Cable Face Pulls with Rope', sets: '4', reps: '15', weight: '20kg' }
      ]},
      { day: 'Wednesday', focus: 'Leg Day (Quads, Hamstrings focus)', exercises: [
        { name: 'Barbell Back Squats (Deep)', sets: '4', reps: '6-8', weight: 'Bar + 35kg each side' },
        { name: 'Romanian Dumbbell Deadlifts', sets: '4', reps: '10', weight: '27.5kg each' },
        { name: 'Bulgarian Split Squats', sets: '3', reps: '10 each', weight: '15kg each' },
        { name: 'Seated Leg Extensions', sets: '3', reps: '15', weight: '50kg' },
        { name: 'Standing Calf Raises', sets: '4', reps: '15', weight: '60kg' }
      ]},
      { day: 'Thursday', focus: 'Core & High Intensity Cardio', exercises: [
        { name: 'Battle Ropes (Intervals)', sets: '5', reps: '45s work / 45s rest', weight: 'Heavy Rope' },
        { name: 'Kettlebell Goblet Squat to Press', sets: '4', reps: '15', weight: '20kg' },
        { name: 'Hanging Knee Raises (Slow)', sets: '3', reps: '15', weight: 'Bodyweight' },
        { name: 'Plank with Shoulder Taps', sets: '3', reps: '1 min', weight: 'Bodyweight' }
      ]},
      { day: 'Friday', focus: 'Hypertrophy Upper Body Split', exercises: [
        { name: 'Dumbbell Incline Press', sets: '4', reps: '10-12', weight: '22.5kg each' },
        { name: 'Lat Pulldowns (Neutral grip)', sets: '3', reps: '10', weight: '50kg' },
        { name: 'Cable Crossover flyes', sets: '3', reps: '15', weight: '12.5kg' },
        { name: 'Standing Bicep EZ Bar Curls', sets: '3', reps: '12', weight: '20kg' }
      ]},
      { day: 'Saturday', focus: 'Posterior Chain & Calves', exercises: [
        { name: 'Conventional Deadlifts', sets: '4', reps: '5', weight: 'Bar + 45kg each side' },
        { name: 'Leg Press (High & Wide)', sets: '3', reps: '12', weight: '150kg' },
        { name: 'Standing Calf Raises', sets: '4', reps: '20', weight: '45kg' }
      ]},
      { day: 'Sunday', focus: 'Rest Day & Active Mobility', exercises: [
        { name: 'Full Rest Day', sets: 'N/A', reps: 'N/A', weight: 'N/A' }
      ]}
    ]
  };

  // Realistic diet plans
  dietChart: any = {
    general: [
      { meal: 'Meal 1 (Breakfast - 08:00 AM)', description: '4 Egg whites + 1 whole egg omelette, 50g oatmeal with skimmed milk, 5 almonds.' },
      { meal: 'Meal 2 (Mid-Morning - 11:30 AM)', description: '1 cup Greek yogurt, 1 medium apple or orange.' },
      { meal: 'Meal 3 (Lunch - 02:00 PM)', description: '120g Grilled chicken breast (or 120g paneer), 80g brown rice, mixed steamed vegetables.' },
      { meal: 'Meal 4 (Pre-Workout - 05:30 PM)', description: '2 Slices whole wheat bread with 1 tbsp peanut butter, 1 cup black coffee.' },
      { meal: 'Meal 5 (Post-Workout - 07:30 PM)', description: '1 Scoop Whey Protein in water, 1 banana.' },
      { meal: 'Meal 6 (Dinner - 09:30 PM)', description: '100g Grilled fish (or soya chunks), large green salad with cucumbers and tomatoes.' }
    ],
    custom: [
      { meal: 'Meal 1 (Breakfast - 07:30 AM)', description: '5 Egg whites + 2 whole eggs scrambled, 70g oats with water, half sliced banana, 10 almonds.' },
      { meal: 'Meal 2 (Mid-Morning - 11:00 AM)', description: '1.5 Scoops Whey Protein with 150ml low-fat milk, 1 cup blueberries.' },
      { meal: 'Meal 3 (Lunch - 01:30 PM)', description: '150g Grilled chicken breast, 120g brown rice, sautéed spinach, mushrooms, and carrots, 1 cup curd.' },
      { meal: 'Meal 4 (Pre-Workout - 05:00 PM)', description: '1 Sweet potato (steamed) + 80g boiled chick-peas, 1 cup green tea.' },
      { meal: 'Meal 5 (Post-Workout - 07:00 PM)', description: '1.5 Scoops Whey Protein in water, 1 large banana or 50g dates.' },
      { meal: 'Meal 6 (Dinner - 09:00 PM)', description: '150g Grilled Salmon (or roasted tofu), stir-fry asparagus and zucchini in olive oil.' }
    ]
  };

  constructor(
    private memberService: MemberService,
    private eshopService: EShopService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadBmiHistory();
    this.loadOrders();
  }

  loadProfile(): void {
    this.memberService.getProfile().subscribe({
      next: data => {
        this.profile = data;
        this.loading = false;
      },
      error: err => {
        this.errorMessage = 'Failed to load profile: ' + (err.error || err.message);
        this.loading = false;
      }
    });
  }

  loadBmiHistory(): void {
    this.memberService.getBmiHistory().subscribe({
      next: data => this.bmiHistory = data,
      error: err => this.errorMessage = 'Failed to load BMI tracking logs.'
    });
  }

  loadOrders(): void {
    this.eshopService.getMyOrders().subscribe({
      next: data => this.orders = data,
      error: err => this.errorMessage = 'Failed to load order logs.'
    });
  }

  calculateBmiValue(): void {
    const heightM = this.heightCm / 100;
    const bmiVal = this.weightKg / (heightM * heightM);
    this.calculatedBmi = parseFloat(bmiVal.toFixed(2));
    
    if (this.calculatedBmi < 18.5) {
      this.bmiCategory = 'Underweight';
    } else if (this.calculatedBmi >= 18.5 && this.calculatedBmi < 25) {
      this.bmiCategory = 'Normal Weight';
    } else if (this.calculatedBmi >= 25 && this.calculatedBmi < 30) {
      this.bmiCategory = 'Overweight';
    } else {
      this.bmiCategory = 'Obese';
    }
  }

  submitBmi(): void {
    this.errorMessage = '';
    this.successMessage = '';
    
    this.memberService.logBmi(this.heightCm, this.weightKg).subscribe({
      next: () => {
        this.successMessage = 'Health status entry logged successfully!';
        this.loadBmiHistory();
        this.calculatedBmi = null;
        this.bmiCategory = '';
      },
      error: err => this.errorMessage = 'Failed to log: ' + (err.error || err.message)
    });
  }
}
