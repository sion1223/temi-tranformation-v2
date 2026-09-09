"""R4 geometry and the nominal full automatic cycle. Standard library only."""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parent
P = json.loads((ROOT / 'parameters.json').read_text(encoding='utf-8'))


def ramp(frame, start, end):
    return max(0.0, min(1.0, (frame - start) / (end - start)))


def pose(frame):
    """Visualisation timeline; actual controller transitions require sensors."""
    if frame <= 865:
        lid = P['lid_open_deg'] * ramp(frame, 13, 265)
        pickup = ramp(frame, 265, 289)
        lift = P['tray_lift_mm'] * ramp(frame, 289, 505)
        tilt = P['tray_tilt_deg'] * ramp(frame, 505, 745)
    else:
        tilt = P['tray_tilt_deg'] * (1-ramp(frame, 865, 1105))
        lift = P['tray_lift_mm'] * (1-ramp(frame, 1105, 1321))
        pickup = 1-ramp(frame, 1321, 1345)
        lid = 105 - 99*ramp(frame, 1369, 1601) - 6*ramp(frame, 1613, 1632)
    released = (13 <= frame <= 35) or (1613 <= frame <= 1632)
    if 1 <= frame < 13:
        bolt = -4.3*ramp(frame, 1, 13)
    elif 35 < frame < 47:
        bolt = -4.3*(1-ramp(frame, 35, 47))
    elif 1601 < frame < 1613:
        bolt = -4.3*ramp(frame, 1601, 1613)
    elif 1632 < frame <= 1644:
        bolt = -4.3*(1-ramp(frame, 1632, 1644))
    else:
        bolt = -4.3 if released else 0.0
    if pickup < 1:
        low = P['shaft_park_z_mm'] + P['low_pickup_clearance_mm']*pickup
        high = P['shaft_park_z_mm'] + P['high_pickup_clearance_mm']*pickup
    else:
        low, high = shaft_heights(lift, tilt)
    return dict(lid_deg=lid, lift_mm=lift, tilt_deg=tilt,
                pickup=pickup, low_z=low, high_z=high, bolt_dx=bolt)


def shaft_heights(lift, tilt_deg):
    t = math.radians(tilt_deg)
    low = P['tray_pivot_mm'][2] + lift
    d = P['high_axis_x_mm'] - P['low_axis_x_mm']
    h = P['tray_min_mm'][2] - P['tray_pivot_mm'][2]
    # Exact tangent of a radius-6 roller to the inclined underside; not a
    # fixed horizontal distance between two rigidly bolted tray hinges.
    high = low + d*math.tan(t) + (h-P['high_roller_radius_mm'])/math.cos(t)
    return low, high


def transform_tray(point, lift, tilt_deg):
    x,y,z = point
    px,_,pz = P['tray_pivot_mm']
    a = math.radians(tilt_deg)
    return (px+(x-px)*math.cos(a)-(z-pz)*math.sin(a), y,
            pz+lift+(x-px)*math.sin(a)+(z-pz)*math.cos(a))


def engineering_summary():
    low, high = shaft_heights(P['tray_lift_mm'], P['tray_tilt_deg'])
    # Both sides must tolerate the full load, including an eccentric stack.
    force = P['load_factor']*(P['paper_design_payload_kg']+P['tray_moving_mass_budget_kg'])*9.81
    screw_torque = force*(P['screw_lead_mm']/1000)/(2*math.pi*P['screw_efficiency_assumed']*P['belt_efficiency_assumed'])
    lid_gravity = P['lid_moving_mass_budget_kg']*9.81*.121
    # Springs can impose 1.50 Nm even where gravitational torque is small.
    design_lid_torque = P['load_factor']*(max(lid_gravity, 1.5)+.15)
    motor_torque = design_lid_torque/(P['lid_transmission_ratio']*P['belt_efficiency_assumed'])
    lo = transform_tray((24,162,51),35,12)
    hi = transform_tray((237.5,162,51),35,12)
    return dict(low_edge_open_mm=lo, high_edge_open_mm=hi,
                low_edge_rise_mm=lo[2]-51, high_edge_rise_mm=hi[2]-51,
                low_screw_travel_mm=low-41.2, high_screw_travel_mm=high-41.2,
                design_force_per_side_N=force,
                required_stepper_running_torque_Nm=screw_torque,
                lid_gravity_Nm=lid_gravity,
                required_lid_motor_running_torque_Nm=motor_torque,
                lid_servo_travel_deg=315,
                selection_note='Running torque and thermal margins require bench measurement; stall/holding torque is not continuous torque.')


if __name__ == '__main__':
    print(json.dumps(engineering_summary(), indent=2))
